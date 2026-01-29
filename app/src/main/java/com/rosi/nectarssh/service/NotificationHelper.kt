package com.rosi.nectarssh.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.rosi.nectarssh.data.ConnectionStatus
import com.rosi.nectarssh.data.SessionState

class NotificationHelper(
    private val context: Context,
    private val notificationManager: NotificationManager
) {
    companion object {
        private const val CHANNEL_ID = "ssh_tunnel_channel"
        private const val CHANNEL_NAME = "SSH Tunnel Connections"
        private const val GROUP_KEY = "com.rosi.nectarssh.TUNNEL_GROUP"
        private const val SUMMARY_ID = 999
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for active SSH tunnel connections"
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildConnectionNotification(sessionState: SessionState, allSessions: List<SessionState>): android.app.Notification {
        val sequence = sessionState.sequenceNumber
        val nickname = sessionState.nickname
        
        val stateText = when (sessionState.status) {
            ConnectionStatus.CONNECTING -> "Connecting"
            ConnectionStatus.CONNECTED -> "Connected"
            ConnectionStatus.DISCONNECTING -> "Disconnecting"
            ConnectionStatus.DISCONNECTED -> "Disconnected"
            ConnectionStatus.ERROR -> "Error"
        }

        // Title format: "#1 My Connection - Connected"
        val title = "$nickname - $stateText"

        val logIntent = Intent().apply {
            setClassName(context, "com.rosi.nectarssh.ui.connection.ConnectionLogActivity")
            putExtra("session_id", sessionState.sessionId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val logPendingIntent = PendingIntent.getActivity(
            context,
            sessionState.notificationId,
            logIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(context, SSHTunnelService::class.java).apply {
            action = SSHTunnelService.ACTION_STOP_SESSION
            putExtra(SSHTunnelService.EXTRA_SESSION_ID, sessionState.sessionId)
        }
        val disconnectPendingIntent = PendingIntent.getService(
            context,
            sessionState.notificationId + 10000,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isOngoing = sessionState.status == ConnectionStatus.CONNECTED || 
                        sessionState.status == ConnectionStatus.CONNECTING ||
                        sessionState.status == ConnectionStatus.ERROR

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(null) // Content text is now part of the title
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(isOngoing)
            .setAutoCancel(false)
            .setContentIntent(logPendingIntent)
            .setGroup(GROUP_KEY)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disconnect",
                disconnectPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /**
     * Build a summary notification to act as the stack header.
     */
    fun buildSummaryNotification(activeSessions: List<SessionState>): android.app.Notification {
        val count = activeSessions.size
        val contentText = if (count == 1) "1 active SSH tunnel" else "$count active SSH tunnels"
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("NectarSSH")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    fun getSummaryId(): Int = SUMMARY_ID
}
