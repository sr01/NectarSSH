package com.rosi.nectarssh.data

import kotlinx.serialization.Serializable

@Serializable
data class PortForward(
    val id: String,
    val connectionId: String,
    val nickname: String,
    val localPort: Int,
    val remoteHost: String,
    val remotePort: Int,
    val enabled: Boolean = true
)
