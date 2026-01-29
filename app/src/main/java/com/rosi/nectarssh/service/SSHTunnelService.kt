package com.rosi.nectarssh.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.rosi.nectarssh.data.ConnectionStatus
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.IdentityStorage
import com.rosi.nectarssh.data.LogEntry
import com.rosi.nectarssh.data.LogLevel
import com.rosi.nectarssh.data.PassphraseRequest
import com.rosi.nectarssh.data.PassphraseResponse
import com.rosi.nectarssh.data.PortForwardStorage
import com.rosi.nectarssh.data.SessionState
import com.rosi.nectarssh.util.PermissionHelper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Parameters
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.password.PasswordFinder
import net.schmizz.sshj.userauth.password.Resource
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class SSHTunnelService : Service() {

    companion object {
        private const val TAG = "SSHTunnelService"
        const val ACTION_START_SESSION = "start_session"
        const val ACTION_STOP_SESSION = "stop_session"
        const val EXTRA_CONNECTION_ID = "connection_id"
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_PORT_FORWARD_ID = "port_forward_id"
    }

    private val activeSessions = ConcurrentHashMap<String, SessionState>()
    private val logFlows = ConcurrentHashMap<String, MutableSharedFlow<LogEntry>>()
    private val sessionStateFlows = ConcurrentHashMap<String, MutableSharedFlow<SessionState>>()
    
    private val passphraseRequestFlow = MutableSharedFlow<PassphraseRequest>()
    private val passphraseResponses = ConcurrentHashMap<String, CompletableDeferred<PassphraseResponse?>>()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationIdCounter = AtomicInteger(1000)
    private val sessionSequenceCounter = AtomicInteger(1)
    private var foregroundNotificationId: Int? = null
    
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private lateinit var notificationHelper: NotificationHelper

    private val binder = TunnelServiceBinder()

    inner class TunnelServiceBinder : Binder() {
        fun getService(): SSHTunnelService = this@SSHTunnelService
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this, notificationManager)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SESSION -> {
                val connectionId = intent.getStringExtra(EXTRA_CONNECTION_ID)
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                val portForwardId = intent.getStringExtra(EXTRA_PORT_FORWARD_ID)
                if (connectionId != null && sessionId != null) {
                    startSession(connectionId, sessionId, portForwardId)
                }
            }
            ACTION_STOP_SESSION -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                if (sessionId != null) {
                    stopSession(sessionId)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun updateSessionState(sessionId: String, transform: (SessionState) -> SessionState) {
        activeSessions[sessionId]?.let { current ->
            val newState = transform(current)
            activeSessions[sessionId] = newState
            updateNotification(newState)
            
            // Emit state update to listeners
            serviceScope.launch {
                sessionStateFlows[sessionId]?.emit(newState)
            }
        }
    }

    fun startSession(connectionId: String, sessionId: String, portForwardId: String? = null) {
        if (activeSessions.containsKey(sessionId)) {
            Log.d(TAG, "Session $sessionId already starting or active")
            return
        }

        serviceScope.launch {
            try {
                val connectionStorage = ConnectionStorage(this@SSHTunnelService)
                val identityStorage = IdentityStorage(this@SSHTunnelService)
                val portForwardStorage = PortForwardStorage(this@SSHTunnelService)

                val connection = connectionStorage.getConnection(connectionId)
                    ?: throw IllegalArgumentException("Connection not found")

                val identity = identityStorage.getIdentity(connection.identityId)
                    ?: throw IllegalArgumentException("Identity not found")

                val portForwards = if (portForwardId != null) {
                    listOfNotNull(portForwardStorage.getPortForward(portForwardId))
                } else {
                    emptyList()
                }

                val nickname = portForwards.firstOrNull()?.nickname ?: connection.nickname
                val notificationId = notificationIdCounter.getAndIncrement()
                val sequenceNumber = sessionSequenceCounter.getAndIncrement()
                
                val sessionState = SessionState(
                    sessionId = sessionId,
                    connectionId = connectionId,
                    nickname = nickname,
                    connection = connection,
                    identity = identity,
                    status = ConnectionStatus.CONNECTING,
                    logs = emptyList(),
                    sshClient = null,
                    session = null,
                    startTime = System.currentTimeMillis(),
                    notificationId = notificationId,
                    sequenceNumber = sequenceNumber,
                    portForwards = portForwards,
                    activeForwarderSockets = emptyList()
                )

                logFlows[sessionId] = MutableSharedFlow(replay = 0)
                sessionStateFlows[sessionId] = MutableSharedFlow(replay = 1)
                activeSessions[sessionId] = sessionState

                updateNotification(sessionState)
                sessionStateFlows[sessionId]?.emit(sessionState)
                
                establishSSHConnection(sessionId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start session", e)
                logToSession(sessionId, LogLevel.ERROR, "Startup failed: ${e.message}")
                updateSessionState(sessionId) { it.copy(status = ConnectionStatus.ERROR) }
            }
        }
    }

    private fun updateNotification(sessionState: SessionState) {
        if (!PermissionHelper.isNotificationPermissionGranted(this)) return

        val notification = notificationHelper.buildConnectionNotification(sessionState, getAllSessions())
        
        synchronized(this) {
            if (foregroundNotificationId == null || foregroundNotificationId == sessionState.notificationId) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ServiceCompat.startForeground(this,
                            sessionState.notificationId,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                        )
                    } else {
                        ServiceCompat.startForeground(this, sessionState.notificationId, notification, 0)
                    }
                    foregroundNotificationId = sessionState.notificationId
                } catch (e: Exception) {
                    Log.e(TAG, "startForeground failed", e)
                    notificationManager.notify(sessionState.notificationId, notification)
                }
            } else {
                notificationManager.notify(sessionState.notificationId, notification)
            }
            
            val sessions = getAllSessions()
            if (sessions.isNotEmpty()) {
                notificationManager.notify(
                    notificationHelper.getSummaryId(),
                    notificationHelper.buildSummaryNotification(sessions)
                )
            }
        }
    }

    fun stopSession(sessionId: String) {
        serviceScope.launch {
            activeSessions[sessionId]?.let { disconnectSSH(it) }
        }
    }

    private suspend fun disconnectSSH(sessionState: SessionState) {
        updateSessionState(sessionState.sessionId) { it.copy(status = ConnectionStatus.DISCONNECTING) }

        try {
            sessionState.activeForwarderSockets.forEach { try { it.close() } catch (e: Exception) {} }
            sessionState.session?.close()
            sessionState.sshClient?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error", e)
        }

        // Set status to DISCONNECTED before removal so UI can react
        updateSessionState(sessionState.sessionId) { it.copy(status = ConnectionStatus.DISCONNECTED) }
        
        // Give UI a tiny moment to process the state change
        delay(200)

        val finalSessionId = sessionState.sessionId
        val finalNotificationId = sessionState.notificationId
        
        activeSessions.remove(finalSessionId)
        logFlows.remove(finalSessionId)
        sessionStateFlows.remove(finalSessionId)
        notificationManager.cancel(finalNotificationId)

        synchronized(this) {
            val sessions = getAllSessions()
            if (sessions.isEmpty()) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                notificationManager.cancel(notificationHelper.getSummaryId())
                foregroundNotificationId = null
                stopSelf()
            } else {
                notificationManager.notify(
                    notificationHelper.getSummaryId(),
                    notificationHelper.buildSummaryNotification(sessions)
                )
                
                if (foregroundNotificationId == finalNotificationId) {
                    foregroundNotificationId = null
                    sessions.firstOrNull()?.let { updateNotification(it) }
                }
            }
        }
    }

    private suspend fun establishSSHConnection(sessionId: String) = withContext(Dispatchers.IO) {
        val sessionState = activeSessions[sessionId] ?: return@withContext
        try {
            logToSession(sessionId, LogLevel.INFO, "Connecting...")
            val ssh = SSHClient()
            ssh.addHostKeyVerifier(PromiscuousVerifier())
            ssh.connect(sessionState.connection.address, sessionState.connection.port)
            
            logToSession(sessionId, LogLevel.INFO, "Server version: ${ssh.transport.serverVersion}")

            if (sessionState.identity.privateKeyPath != null) {
                authenticateWithPrivateKey(ssh, sessionId)
            } else if (sessionState.identity.password != null) {
                ssh.authPassword(sessionState.identity.username, sessionState.identity.password)
            }

            logToSession(sessionId, LogLevel.INFO, "Authentication successful")

            val session = ssh.startSession()
            session.allocatePTY("vt100", 120, 40, 0, 0, emptyMap())
            
            updateSessionState(sessionId) { 
                it.copy(sshClient = ssh, session = session)
            }

            // Start capturing remote output before starting shell
            startCapturingOutput(sessionId, session)
            
            session.startShell()

            val currentPf = activeSessions[sessionId]?.portForwards ?: emptyList()
            if (currentPf.isNotEmpty()) {
                setupPortForwards(ssh, sessionId)
            }

            updateSessionState(sessionId) { it.copy(status = ConnectionStatus.CONNECTED) }
            logToSession(sessionId, LogLevel.INFO, "Connected and ready")

            ssh.connection.keepAlive.keepAliveInterval = 30
            while (isActive && ssh.isConnected) { delay(5000) }

            if (isActive) {
                logToSession(sessionId, LogLevel.WARNING, "Lost connection")
                updateSessionState(sessionId) { it.copy(status = ConnectionStatus.ERROR) }
            }
        } catch (e: Exception) {
            logToSession(sessionId, LogLevel.ERROR, "Error: ${e.message}")
            if (e is java.net.BindException) {
                activeSessions[sessionId]?.let { disconnectSSH(it) }
            } else {
                updateSessionState(sessionId) { it.copy(status = ConnectionStatus.ERROR) }
            }
        }
    }

    private fun startCapturingOutput(sessionId: String, session: Session) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                InputStreamReader(session.inputStream).use { reader ->
                    val buffer = CharArray(4096)
                    var count: Int = 0
                    while (isActive && reader.read(buffer).also { count = it } != -1) {
                        val output = String(buffer, 0, count)
                        logToSession(sessionId, LogLevel.DEBUG, output)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Input stream closed for session $sessionId")
            }
        }
    }

    private fun setupPortForwards(ssh: SSHClient, sessionId: String) {
        val sessionState = activeSessions[sessionId] ?: return
        val sockets = mutableListOf<ServerSocket>()
        
        try {
            for (pf in sessionState.portForwards) {
                val serverSocket = ServerSocket()
                serverSocket.reuseAddress = true
                try {
                    serverSocket.bind(InetSocketAddress("127.0.0.1", pf.localPort))
                } catch (e: java.net.BindException) {
                    serverSocket.close()
                    throw java.net.BindException("Local port ${pf.localPort} is already in use")
                }
                
                sockets.add(serverSocket)
                val params = Parameters("127.0.0.1", serverSocket.localPort, pf.remoteHost, pf.remotePort)

                Thread {
                    try {
                        ssh.newLocalPortForwarder(params, serverSocket).listen()
                    } catch (e: Exception) {
                        if (!serverSocket.isClosed) {
                            serviceScope.launch {
                                logToSession(sessionId, LogLevel.ERROR, "Port forward ${pf.localPort} error: ${e.message}")
                            }
                        }
                    }
                }.start()
                logToSession(sessionId, LogLevel.INFO, "Forward: ${pf.localPort} -> ${pf.remoteHost}:${pf.remotePort}")
            }
            updateSessionState(sessionId) { it.copy(activeForwarderSockets = it.activeForwarderSockets + sockets) }
        } catch (e: Exception) {
            sockets.forEach { try { it.close() } catch (ex: Exception) {} }
            throw e
        }
    }

    private suspend fun authenticateWithPrivateKey(ssh: SSHClient, sessionId: String) {
        val sessionState = activeSessions[sessionId] ?: return
        val keyPath = sessionState.identity.privateKeyPath ?: return
        val savedPassphrase = sessionState.identity.privateKeyPassphrase

        try {
            val finder = if (savedPassphrase != null) {
                object : PasswordFinder {
                    override fun reqPassword(resource: Resource<*>?): CharArray = savedPassphrase.toCharArray()
                    override fun shouldRetry(resource: Resource<*>?): Boolean = false
                }
            } else null

            ssh.authPublickey(sessionState.identity.username, if (finder != null) ssh.loadKeys(keyPath, finder) else ssh.loadKeys(keyPath))
        } catch (e: Exception) {
            if (e.message?.contains("passphrase") == true) {
                val response = requestPassphrase(sessionId, sessionState.identity.id)
                if (response?.passphrase != null) {
                    val finder = object : PasswordFinder {
                        override fun reqPassword(resource: Resource<*>?): CharArray = response.passphrase.toCharArray()
                        override fun shouldRetry(resource: Resource<*>?): Boolean = false
                    }
                    ssh.authPublickey(sessionState.identity.username, ssh.loadKeys(keyPath, finder))
                    if (response.savePassphrase) savePassphrase(sessionState.identity.id, response.passphrase)
                } else throw IllegalStateException("Passphrase required")
            } else throw e
        }
    }

    private suspend fun requestPassphrase(sessionId: String, identityId: String): PassphraseResponse? {
        val deferred = CompletableDeferred<PassphraseResponse?>()
        passphraseResponses[sessionId] = deferred
        passphraseRequestFlow.emit(PassphraseRequest(sessionId, identityId))
        return withTimeoutOrNull(60000) { deferred.await() }.also { passphraseResponses.remove(sessionId) }
    }

    private fun savePassphrase(id: String, pass: String) {
        val storage = IdentityStorage(this)
        storage.getIdentity(id)?.let { storage.updateIdentity(it.copy(privateKeyPassphrase = pass)) }
    }

    fun getSessionState(sessionId: String): SessionState? = activeSessions[sessionId]
    fun getAllSessions(): List<SessionState> = activeSessions.values.toList().sortedBy { it.startTime }
    fun getLogFlow(sessionId: String): SharedFlow<LogEntry>? = logFlows[sessionId]
    fun getSessionStateFlow(sessionId: String): SharedFlow<SessionState>? = sessionStateFlows[sessionId]?.asSharedFlow()
    fun getPassphraseRequestFlow(): SharedFlow<PassphraseRequest> = passphraseRequestFlow
    fun providePassphrase(response: PassphraseResponse) { passphraseResponses[response.sessionId]?.complete(response) }

    private fun logToSession(sessionId: String, level: LogLevel, message: String) {
        val entry = LogEntry(System.currentTimeMillis(), level, message)
        activeSessions[sessionId]?.let { current ->
            val newLogs = current.logs + entry
            val newState = current.copy(logs = if (newLogs.size > 1000) newLogs.drop(1) else newLogs)
            activeSessions[sessionId] = newState
            
            serviceScope.launch {
                sessionStateFlows[sessionId]?.emit(newState)
            }
        }
        logFlows[sessionId]?.tryEmit(entry)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            activeSessions.values.forEach { state ->
                state.activeForwarderSockets.forEach { try { it.close() } catch (e: Exception) {} }
                state.session?.close()
                state.sshClient?.disconnect()
            }
            activeSessions.clear()
        }
        serviceScope.cancel()
    }
}
