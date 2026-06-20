package com.rosi.nectarssh.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.rosi.nectarssh.R
import com.rosi.nectarssh.ShortcutLaunchActivity

object ShortcutHelper {

    fun requestPinShortcut(
        context: Context,
        itemId: String,
        shortcutType: String,
        label: String,
        icon: IconCompat? = null
    ): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val shortcutIntent = Intent(context, ShortcutLaunchActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(ShortcutLaunchActivity.EXTRA_SHORTCUT_TYPE, shortcutType)
            putExtra(ShortcutLaunchActivity.EXTRA_ITEM_ID, itemId)
        }

        val uniqueId = "nectar_${shortcutType}_${itemId}_${System.currentTimeMillis()}"
        val shortcutInfo = ShortcutInfoCompat.Builder(context, uniqueId)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon ?: IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(shortcutIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}
