package com.rosi.nectarssh.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
enum class RecentType {
    CONNECTION,
    PORT_FORWARD
}

@Serializable
data class RecentItem(
    val id: String,
    val type: RecentType,
    val lastUsed: Long = System.currentTimeMillis()
)

class RecentStorage(private val context: Context) {
    private val recentFile = File(context.filesDir, "recent.json")

    fun loadRecentItems(): List<RecentItem> {
        return try {
            if (recentFile.exists()) {
                val json = recentFile.readText()
                Json.decodeFromString(json)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveRecentItems(items: List<RecentItem>) {
        try {
            val json = Json.encodeToString(items)
            recentFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addRecentItem(id: String, type: RecentType) {
        val items = loadRecentItems().toMutableList()
        // Remove if already exists to update timestamp
        items.removeAll { it.id == id && it.type == type }
        // Add at the beginning
        items.add(0, RecentItem(id, type))
        // Keep only up to 10 items (more than 5 just in case)
        val result = if (items.size > 10) items.take(10) else items
        saveRecentItems(result)
    }
}
