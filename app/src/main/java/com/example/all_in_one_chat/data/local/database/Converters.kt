package com.example.all_in_one_chat.data.local.database

import androidx.room.TypeConverter
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.MediaType

class Converters {
    @TypeConverter
    fun fromChatServiceType(value: ChatServiceType): String {
        return value.name
    }

    @TypeConverter
    fun toChatServiceType(value: String): ChatServiceType {
        return ChatServiceType.valueOf(value)
    }

    @TypeConverter
    fun fromMediaType(value: MediaType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toMediaType(value: String?): MediaType? {
        return value?.let { MediaType.valueOf(it) }
    }
}
