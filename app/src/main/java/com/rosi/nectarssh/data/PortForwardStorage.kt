package com.rosi.nectarssh.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PortForwardStorage(private val context: Context) {
    private val portForwardsFile = File(context.filesDir, "port_forwards.json")

    fun loadPortForwards(): List<PortForward> {
        return try {
            if (portForwardsFile.exists()) {
                val json = portForwardsFile.readText()
                Json.decodeFromString(json)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun savePortForwards(portForwards: List<PortForward>) {
        try {
            val json = Json.encodeToString(portForwards)
            portForwardsFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addPortForward(portForward: PortForward): List<PortForward> {
        val portForwards = loadPortForwards().toMutableList()
        portForwards.add(portForward)
        savePortForwards(portForwards)
        return portForwards
    }

    fun updatePortForward(portForward: PortForward): List<PortForward> {
        val portForwards = loadPortForwards().toMutableList()
        val index = portForwards.indexOfFirst { it.id == portForward.id }
        if (index != -1) {
            portForwards[index] = portForward
            savePortForwards(portForwards)
        }
        return portForwards
    }

    fun getPortForward(portForwardId: String): PortForward? {
        return loadPortForwards().find { it.id == portForwardId }
    }

    fun getPortForwardsForConnection(connectionId: String): List<PortForward> {
        return loadPortForwards().filter { it.connectionId == connectionId }
    }

    fun deletePortForward(portForwardId: String): List<PortForward> {
        val portForwards = loadPortForwards().toMutableList()
        portForwards.removeAll { it.id == portForwardId }
        savePortForwards(portForwards)
        return portForwards
    }

    fun deletePortForwardsForConnection(connectionId: String): List<PortForward> {
        val portForwards = loadPortForwards().toMutableList()
        portForwards.removeAll { it.connectionId == connectionId }
        savePortForwards(portForwards)
        return portForwards
    }
}
