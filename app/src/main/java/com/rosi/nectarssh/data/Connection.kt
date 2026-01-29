package com.rosi.nectarssh.data

import kotlinx.serialization.Serializable

@Serializable
data class Connection(
    val id: String,
    val nickname: String,
    val address: String,
    val port: Int,
    val identityId: String
)
