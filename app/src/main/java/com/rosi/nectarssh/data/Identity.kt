package com.rosi.nectarssh.data

import kotlinx.serialization.Serializable

@Serializable
data class Identity(
    val id: String,
    val nickname: String,
    val username: String,
    val password: String? = null,
    val privateKeyPath: String? = null,
    val privateKeyPassphrase: String? = null
)
