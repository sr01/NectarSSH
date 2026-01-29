package com.rosi.nectarssh.ui.connection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        if (sessionId == null) {
            finish()
            return
        }

        // Bind to service
        val intent = Intent(this, SSHTunnelService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        setContent {
            NectarSSHTheme {
                ConnectionLogScreen(
                    sessionId = sessionId!!,
                    getSessionState = { tunnelService?.getSessionState(sessionId!!) },
                    getSessionStateFlow = { tunnelService?.getSessionStateFlow(sessionId!!) },
                    getLogFlow = { tunnelService?.getLogFlow(sessionId!!) },
                    getPassphraseRequestFlow = { tunnelService?.getPassphraseRequestFlow() },
                    onPassphraseResponse = { response ->
                        tunnelService?.providePassphrase(response)
                    },
                    onDisconnect = {
                        tunnelService?.stopSession(sessionId!!)
                        finish()
                    },
                    onKeepRunning = {
                        finish()
                    },
                    onBack = {
                        handleBack()
                    }
                )
            }
        }
    }

    private fun handleBack() {
        val sessionState = tunnelService?.getSessionState(sessionId!!)
        if (sessionState?.status == ConnectionStatus.CONNECTED || sessionState?.status == ConnectionStatus.CONNECTING) {
            // Will be handled by ConnectionLogScreen's dialog
        } else {
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
