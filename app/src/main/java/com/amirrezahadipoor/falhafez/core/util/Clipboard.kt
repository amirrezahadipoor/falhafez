package com.amirrezahadipoor.falhafez.core.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object Clipboard {
    fun copy(context: Context, label: String, text: String) {
        val manager = context.getSystemService(ClipboardManager::class.java)
        manager?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
    }
}
