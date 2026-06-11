package com.rosi.nectarssh.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PortForwardGroupStorage(private val context: Context) {
    private val groupsFile = File(context.filesDir, "port_forward_groups.json")

    fun loadGroups(): List<PortForwardGroup> {
        return try {
            if (groupsFile.exists()) {
                val json = groupsFile.readText()
                Json.decodeFromString(json)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveGroups(groups: List<PortForwardGroup>) {
        try {
            val json = Json.encodeToString(groups)
            groupsFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addGroup(group: PortForwardGroup): List<PortForwardGroup> {
        val groups = loadGroups().toMutableList()
        groups.add(group)
        saveGroups(groups)
        return groups
    }

    fun updateGroup(group: PortForwardGroup): List<PortForwardGroup> {
        val groups = loadGroups().toMutableList()
        val index = groups.indexOfFirst { it.id == group.id }
        if (index != -1) {
            groups[index] = group
            saveGroups(groups)
        }
        return groups
    }

    fun getGroup(groupId: String): PortForwardGroup? {
        return loadGroups().find { it.id == groupId }
    }

    fun getGroupsForConnection(connectionId: String): List<PortForwardGroup> {
        return loadGroups().filter { it.connectionId == connectionId }
    }

    fun deleteGroup(groupId: String): List<PortForwardGroup> {
        val groups = loadGroups().toMutableList()
        groups.removeAll { it.id == groupId }
        saveGroups(groups)
        return groups
    }

    fun deleteGroupsForConnection(connectionId: String): List<PortForwardGroup> {
        val groups = loadGroups().toMutableList()
        groups.removeAll { it.connectionId == connectionId }
        saveGroups(groups)
        return groups
    }
}
