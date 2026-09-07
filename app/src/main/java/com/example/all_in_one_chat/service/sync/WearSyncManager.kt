package com.example.all_in_one_chat.service.sync

import android.content.Context
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataClient = Wearable.getDataClient(context)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ChatRoom::class.java)
    private val adapter = moshi.adapter<List<ChatRoom>>(listType)

    suspend fun syncChatRooms(rooms: List<ChatRoom>) {
        try {
            val json = adapter.toJson(rooms.take(10)) // Send only top 10 for performance
            val request = PutDataMapRequest.create("/chat_rooms").apply {
                dataMap.putString("rooms_json", json)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()

            dataClient.putDataItem(request).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
