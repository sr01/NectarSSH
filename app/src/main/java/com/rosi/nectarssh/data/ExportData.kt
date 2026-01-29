package com.rosi.nectarssh.data

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val version: Int = 1,
    val exportDate: String,
    val identities: List<ExportIdentity>,
    val connections: List<Connection>,
    val portForwards: List<PortForward>
)

@Serializable
data class ExportIdentity(
    val id: String,
    val nickname: String,
    val username: String,
    val password: String? = null,
    val privateKeyData: String? = null,  // Base64 encoded key file content
    val privateKeyPassphrase: String? = null
)

@Serializable
data class ImportResult(
    val success: Boolean,
    val message: String,
    val identityCount: Int = 0,
    val connectionCount: Int = 0,
    val portForwardCount: Int = 0
)
