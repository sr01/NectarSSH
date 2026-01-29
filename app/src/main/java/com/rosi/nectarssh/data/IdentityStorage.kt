package com.rosi.nectarssh.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class IdentityStorage(private val context: Context) {
    private val identitiesFile = File(context.filesDir, "identities.json")
    private val keysDir = File(context.filesDir, "keys").apply { mkdirs() }

    fun loadIdentities(): List<Identity> {
        return try {
            if (identitiesFile.exists()) {
                val json = identitiesFile.readText()
                Json.decodeFromString(json)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveIdentities(identities: List<Identity>) {
        try {
            val json = Json.encodeToString(identities)
            identitiesFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addIdentity(identity: Identity): List<Identity> {
        val identities = loadIdentities().toMutableList()
        identities.add(identity)
        saveIdentities(identities)
        return identities
    }

    fun updateIdentity(identity: Identity): List<Identity> {
        val identities = loadIdentities().toMutableList()
        val index = identities.indexOfFirst { it.id == identity.id }
        if (index != -1) {
            identities[index] = identity
            saveIdentities(identities)
        }
        return identities
    }

    fun getIdentity(identityId: String): Identity? {
        return loadIdentities().find { it.id == identityId }
    }

    fun deleteIdentity(identityId: String): List<Identity> {
        val identities = loadIdentities().toMutableList()
        val identity = identities.find { it.id == identityId }

        // Delete associated private key file if exists
        identity?.privateKeyPath?.let { path ->
            File(path).delete()
        }

        identities.removeAll { it.id == identityId }
        saveIdentities(identities)
        return identities
    }

    fun savePrivateKey(content: ByteArray): String {
        val filename = "key_${UUID.randomUUID()}"
        val keyFile = File(keysDir, filename)
        keyFile.writeBytes(content)
        return keyFile.absolutePath
    }
}
