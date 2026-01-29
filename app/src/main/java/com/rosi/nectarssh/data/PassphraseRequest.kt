package com.rosi.nectarssh.data

data class PassphraseRequest(
    val sessionId: String,
    val identityId: String
)

data class PassphraseResponse(
    val sessionId: String,
    val passphrase: String?,
    val savePassphrase: Boolean
)
