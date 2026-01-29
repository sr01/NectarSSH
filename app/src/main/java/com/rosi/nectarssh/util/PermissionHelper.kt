package com.rosi.nectarssh.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_PERMISSION_DECLINED = "notification_permission_declined"

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun shouldRequestNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            !isNotificationPermissionGranted(context) &&
            !wasPermissionRequestDeclined(context)
        } else {
            false
        }
    }

    fun markPermissionRequestDeclined(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PERMISSION_DECLINED, true)
            .apply()
    }

    private fun wasPermissionRequestDeclined(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PERMISSION_DECLINED, false)
    }

    /**
     * Get the required storage permissions for the current Android version.
     * Only requesting READ_EXTERNAL_STORAGE for all versions that need it.
     */
    fun getRequiredStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            emptyArray()
        }
    }

    /**
     * Check if the app has the necessary storage permissions.
     */
    fun isStoragePermissionGranted(context: Context): Boolean {
        val permissions = getRequiredStoragePermissions()
        if (permissions.isEmpty()) return true
        
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
