package com.rosi.nectarssh.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID

object ShortcutIconStorage {

    private const val ICONS_DIR = "shortcut_icons"

    private fun getIconsDir(context: Context): File {
        return File(context.filesDir, ICONS_DIR).also { it.mkdirs() }
    }

    fun saveIconFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val size = minOf(original.width, original.height)
            val x = (original.width - size) / 2
            val y = (original.height - size) / 2
            val cropped = Bitmap.createBitmap(original, x, y, size, size)

            val scaled = Bitmap.createScaledBitmap(cropped, 192, 192, true)
            if (cropped != original) cropped.recycle()
            if (original != cropped) original.recycle()

            val file = File(getIconsDir(context), "${UUID.randomUUID()}.png")
            file.outputStream().use { scaled.compress(Bitmap.CompressFormat.PNG, 100, it) }

            scaled
        } catch (e: Exception) {
            null
        }
    }
}
