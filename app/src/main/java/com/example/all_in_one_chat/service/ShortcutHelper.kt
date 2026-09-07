package com.example.all_in_one_chat.service

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.all_in_one_chat.R
import com.example.all_in_one_chat.domain.model.ChatRoom
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

@Singleton
class ShortcutHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun updateShortcuts(rooms: List<ChatRoom>) {
        val shortcuts = rooms.take(4).map { room ->
            val intent = Intent(
                Intent.ACTION_VIEW,
                "all-in-one-chat://chat/${room.serviceType.name}/${room.roomId}".toUri()
            )

            ShortcutInfoCompat.Builder(context, room.roomId)
                .setShortLabel(room.roomName)
                .setLongLabel(room.roomName)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_notification)) // Fallback icon
                .setIntent(intent)
                .build()
        }

        ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts)
    }
}
