package com.rosi.nectarssh.data

import kotlinx.serialization.Serializable

@Serializable
data class PortForwardGroup(
    val id: String,
    val connectionId: String,
    val nickname: String,
    val portForwardIds: List<String>
)
