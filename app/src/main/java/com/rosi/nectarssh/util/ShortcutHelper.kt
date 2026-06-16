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
        label: String
    ): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val shortcutIntent = Intent(context, ShortcutLaunchActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(ShortcutLaunchActivity.EXTRA_SHORTCUT_TYPE, shortcutType)
            putExtra(ShortcutLaunchActivity.EXTRA_ITEM_ID, itemId)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "nectar_${shortcutType}_$itemId")
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(shortcutIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}
