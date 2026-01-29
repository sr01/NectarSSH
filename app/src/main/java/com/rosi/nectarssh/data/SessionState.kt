package com.rosi.nectarssh.data

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import java.net.ServerSocket

data class SessionState(
    val sessionId: String,          // Unique ID for THIS session instance (UUID)
    val connectionId: String,        // The connection being used (can have multiple sessions)
    val nickname: String,            // Display name (Connection or Port Forward nickname)
    val connection: Connection,
    val identity: Identity,
    val status: ConnectionStatus,
    val logs: List<LogEntry>,
    val sshClient: SSHClient?,
    val session: Session?,
    val startTime: Long,
    val notificationId: Int,
    val sequenceNumber: Int,         // Stable sequential number for notification display
    val portForwards: List<PortForward> = emptyList(),
    val activeForwarderSockets: List<ServerSocket> = emptyList()
)

enum class ConnectionStatus {
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    DISCONNECTED,
    ERROR
}
