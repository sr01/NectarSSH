package com.rosi.nectarssh.ui.connection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rosi.nectarssh.data.ConnectionStatus
import com.rosi.nectarssh.service.SSHTunnelService
import com.rosi.nectarssh.ui.theme.NectarSSHTheme

class ConnectionLogActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SESSION_ID = "session_id"
    }

    private var tunnelService by mutableStateOf<SSHTunnelService?>(null)
    private var serviceBound = false
    private var sessionId: String? = null
    private var showTerminal by mutableStateOf(false)
    private var showDisconnectDialog by mutableStateOf(false)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            tunnelService = (binder as? SSHTunnelService.TunnelServiceBinder)?.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            tunnelService = null
            serviceBound = false
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        if (sessionId == null) {
            finish()
            return
        }

        val intent = Intent(this, SSHTunnelService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        setContent {
            NectarSSHTheme {
                val service = tunnelService
                val inputStream = if (showTerminal) service?.getSessionInputStream(sessionId!!) else null
                val outputStream = if (showTerminal) service?.getSessionOutputStream(sessionId!!) else null

                if (showTerminal && inputStream != null && outputStream != null) {
                    BackHandler { showDisconnectDialog = true }
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(service?.getSessionState(sessionId!!)?.nickname ?: "Terminal") },
                                navigationIcon = {
                                    IconButton(onClick = { showDisconnectDialog = true }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            )
                        }
                    ) { padding ->
                        TerminalScreen(
                            inputStream = inputStream,
                            outputStream = outputStream,
                            onResize = { cols, rows ->
                                service?.resizeTerminal(sessionId!!, cols, rows)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        )
                    }

                    if (showDisconnectDialog) {
                        AlertDialog(
                            onDismissRequest = { showDisconnectDialog = false },
                            title = { Text("Connection Active") },
                            text = { Text("Keep SSH connection running in background?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDisconnectDialog = false
                                    service?.stopSession(sessionId!!)
                                    finish()
                                }) { Text("Disconnect") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showDisconnectDialog = false
                                    finish()
                                }) { Text("Keep Running") }
                            }
                        )
                    }
                } else {
                    ConnectionLogScreen(
                        sessionId = sessionId!!,
                        getSessionState = { service?.getSessionState(sessionId!!) },
                        getSessionStateFlow = { service?.getSessionStateFlow(sessionId!!) },
                        getLogFlow = { service?.getLogFlow(sessionId!!) },
                        getPassphraseRequestFlow = { service?.getPassphraseRequestFlow() },
                        onPassphraseResponse = { response ->
                            service?.providePassphrase(response)
                        },
                        onDisconnect = {
                            service?.stopSession(sessionId!!)
                            finish()
                        },
                        onKeepRunning = {
                            finish()
                        },
                        onBack = {
                            handleBack()
                        },
                        onConnected = {
                            android.util.Log.d("NectarTerminal", "onConnected called, switching to terminal")
                            showTerminal = true
                        },
                        serviceReady = service != null
                    )
                }
            }
        }
    }

    private fun handleBack() {
        val sessionState = tunnelService?.getSessionState(sessionId!!)
        android.util.Log.d("NectarTerminal", "handleBack called, status=${sessionState?.status}")
        if (sessionState?.status == ConnectionStatus.CONNECTED || sessionState?.status == ConnectionStatus.CONNECTING) {
            // Will be handled by ConnectionLogScreen's dialog
        } else {
            if (sessionState?.status == ConnectionStatus.ERROR) {
                tunnelService?.stopSession(sessionId!!)
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
