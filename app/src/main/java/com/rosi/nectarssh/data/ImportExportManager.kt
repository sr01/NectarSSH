package com.rosi.nectarssh.data

import android.content.Context
import android.util.Base64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant

object ImportExportManager {

    /**
     * Export all data (connections, identities, port forwards, SSH keys) to JSON string
     */
    fun exportAllData(context: Context): String {
        val identityStorage = IdentityStorage(context)
        val connectionStorage = ConnectionStorage(context)
        val portForwardStorage = PortForwardStorage(context)

        // Load all identities and convert to ExportIdentity with base64-encoded keys
        val exportIdentities = identityStorage.loadIdentities().map { identity ->
            val keyData = identity.privateKeyPath?.let { path ->
                val keyFile = File(path)
                if (keyFile.exists()) {
                    Base64.encodeToString(keyFile.readBytes(), Base64.NO_WRAP)
                } else null
            }

            ExportIdentity(
                id = identity.id,
                nickname = identity.nickname,
                username = identity.username,
                password = identity.password,
                privateKeyData = keyData,
                privateKeyPassphrase = identity.privateKeyPassphrase
            )
        }

        val exportData = ExportData(
            version = 1,
            exportDate = Instant.now().toString(),
            identities = exportIdentities,
            connections = connectionStorage.loadConnections(),
            portForwards = portForwardStorage.loadPortForwards()
        )

        return Json.encodeToString(exportData)
    }

    /**
     * Import data from JSON string, replacing ALL existing data
     */
    fun importData(context: Context, jsonContent: String): ImportResult {
        return try {
            // 1. Parse and validate JSON
            val exportData = Json.decodeFromString<ExportData>(jsonContent)

            if (exportData.version != 1) {
                return ImportResult(
                    success = false,
                    message = "Unsupported export version: ${exportData.version}"
                )
            }

            val identityStorage = IdentityStorage(context)
            val connectionStorage = ConnectionStorage(context)
            val portForwardStorage = PortForwardStorage(context)

            // 2. Clear ALL existing data
            // Delete all identities (including their key files)
            identityStorage.loadIdentities().forEach { identity ->
                identityStorage.deleteIdentity(identity.id)
            }

            // Clear connections and port forwards
            connectionStorage.saveConnections(emptyList())
            portForwardStorage.savePortForwards(emptyList())

            // 3. Import identities with key reconstruction
            exportData.identities.forEach { exportIdentity ->
                // Reconstruct private key file from base64 data
                val privateKeyPath = exportIdentity.privateKeyData?.let { base64Data ->
                    val keyBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    identityStorage.savePrivateKey(keyBytes)
                }

                val identity = Identity(
                    id = exportIdentity.id,
                    nickname = exportIdentity.nickname,
                    username = exportIdentity.username,
                    password = exportIdentity.password,
                    privateKeyPath = privateKeyPath,
                    privateKeyPassphrase = exportIdentity.privateKeyPassphrase
                )
                identityStorage.addIdentity(identity)
            }

            // 4. Import connections
            connectionStorage.saveConnections(exportData.connections)

            // 5. Import port forwards
            portForwardStorage.savePortForwards(exportData.portForwards)

            ImportResult(
                success = true,
                message = "Import successful",
                identityCount = exportData.identities.size,
                connectionCount = exportData.connections.size,
                portForwardCount = exportData.portForwards.size
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                success = false,
                message = "Import failed: ${e.message ?: "Unknown error"}"
            )
        }
    }

    /**
     * Validate import JSON without actually importing
     */
    fun validateImportData(jsonContent: String): ImportResult {
        return try {
            val exportData = Json.decodeFromString<ExportData>(jsonContent)

            if (exportData.version != 1) {
                ImportResult(
                    success = false,
                    message = "Unsupported export version: ${exportData.version}"
                )
            } else {
                ImportResult(
                    success = true,
                    message = "Valid import data",
                    identityCount = exportData.identities.size,
                    connectionCount = exportData.connections.size,
                    portForwardCount = exportData.portForwards.size
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                success = false,
                message = "Invalid JSON: ${e.message ?: "Unknown error"}"
            )
        }
    }
}
