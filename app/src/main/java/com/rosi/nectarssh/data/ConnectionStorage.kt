package com.rosi.nectarssh.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ConnectionStorage(private val context: Context) {
    private val connectionsFile = File(context.filesDir, "connections.json")

    fun loadConnections(): List<Connection> {
        return try {
            if (connectionsFile.exists()) {
                val json = connectionsFile.readText()
                Json.decodeFromString(json)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveConnections(connections: List<Connection>) {
        try {
            val json = Json.encodeToString(connections)
            connectionsFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addConnection(connection: Connection): List<Connection> {
        val connections = loadConnections().toMutableList()
        connections.add(connection)
        saveConnections(connections)
        return connections
    }

    fun updateConnection(connection: Connection): List<Connection> {
        val connections = loadConnections().toMutableList()
        val index = connections.indexOfFirst { it.id == connection.id }
        if (index != -1) {
            connections[index] = connection
            saveConnections(connections)
        }
        return connections
    }

    fun getConnection(connectionId: String): Connection? {
        return loadConnections().find { it.id == connectionId }
    }

    fun deleteConnection(connectionId: String): List<Connection> {
        val connections = loadConnections().toMutableList()
        connections.removeAll { it.id == connectionId }
        saveConnections(connections)
        return connections
    }
}
