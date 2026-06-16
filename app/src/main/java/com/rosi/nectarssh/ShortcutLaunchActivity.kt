package com.rosi.nectarssh

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.PortForwardGroupStorage
import com.rosi.nectarssh.data.PortForwardStorage
import com.rosi.nectarssh.data.RecentStorage
import com.rosi.nectarssh.data.RecentType
import com.rosi.nectarssh.service.SSHTunnelService
import java.util.UUID

class ShortcutLaunchActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SHORTCUT_TYPE = "shortcut_type"
        const val EXTRA_ITEM_ID = "item_id"

        const val TYPE_CONNECTION = "connection"
        const val TYPE_PORT_FORWARD = "port_forward"
        const val TYPE_PORT_FORWARD_GROUP = "port_forward_group"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutType = intent.getStringExtra(EXTRA_SHORTCUT_TYPE)
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID)

        if (shortcutType == null || itemId == null) {
            finish()
            return
        }

        when (shortcutType) {
            TYPE_CONNECTION -> launchConnection(itemId)
            TYPE_PORT_FORWARD -> launchPortForward(itemId)
            TYPE_PORT_FORWARD_GROUP -> launchGroup(itemId)
        }

        finish()
    }

    private fun launchConnection(connectionId: String) {
        val connectionStorage = ConnectionStorage(this)
        val connection = connectionStorage.getConnection(connectionId) ?: return

        val recentStorage = RecentStorage(this)
        recentStorage.addRecentItem(connection.id, RecentType.CONNECTION)

        val sessionId = UUID.randomUUID().toString()
        startTunnelService(connection.id, sessionId)
        openConnectionLog(sessionId)
    }

    private fun launchPortForward(portForwardId: String) {
        val portForwardStorage = PortForwardStorage(this)
        val pf = portForwardStorage.getPortForward(portForwardId) ?: return

        val recentStorage = RecentStorage(this)
        recentStorage.addRecentItem(pf.id, RecentType.PORT_FORWARD)

        val sessionId = UUID.randomUUID().toString()
        val serviceIntent = Intent(this, SSHTunnelService::class.java).apply {
            action = SSHTunnelService.ACTION_START_SESSION
            putExtra(SSHTunnelService.EXTRA_CONNECTION_ID, pf.connectionId)
            putExtra(SSHTunnelService.EXTRA_SESSION_ID, sessionId)
            putExtra(SSHTunnelService.EXTRA_PORT_FORWARD_ID, pf.id)
        }
        startService(serviceIntent)
        openConnectionLog(sessionId)
    }

    private fun launchGroup(groupId: String) {
        val groupStorage = PortForwardGroupStorage(this)
        val group = groupStorage.getGroup(groupId) ?: return

        val recentStorage = RecentStorage(this)
        recentStorage.addRecentItem(group.id, RecentType.PORT_FORWARD_GROUP)

        val sessionId = UUID.randomUUID().toString()
        val serviceIntent = Intent(this, SSHTunnelService::class.java).apply {
            action = SSHTunnelService.ACTION_START_SESSION
            putExtra(SSHTunnelService.EXTRA_CONNECTION_ID, group.connectionId)
            putExtra(SSHTunnelService.EXTRA_SESSION_ID, sessionId)
            putExtra(SSHTunnelService.EXTRA_PORT_FORWARD_GROUP_ID, group.id)
        }
        startService(serviceIntent)
        openConnectionLog(sessionId)
    }

    private fun startTunnelService(connectionId: String, sessionId: String) {
        val serviceIntent = Intent(this, SSHTunnelService::class.java).apply {
            action = SSHTunnelService.ACTION_START_SESSION
            putExtra(SSHTunnelService.EXTRA_CONNECTION_ID, connectionId)
            putExtra(SSHTunnelService.EXTRA_SESSION_ID, sessionId)
        }
        startService(serviceIntent)
    }

    private fun openConnectionLog(sessionId: String) {
        val logIntent = Intent().apply {
            setClassName(this@ShortcutLaunchActivity, "com.rosi.nectarssh.ui.connection.ConnectionLogActivity")
            putExtra("session_id", sessionId)
        }
        startActivity(logIntent)
    }
}
