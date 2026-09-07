package com.example.all_in_one_chat.service

import com.example.all_in_one_chat.domain.model.ChatRoom
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WearDataLayerService : WearableListenerService() {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ChatRoom::class.java)
    private val adapter = moshi.adapter<List<ChatRoom>>(listType)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.dataItem.uri.path == "/chat_rooms") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val roomsJson = dataMap.getString("rooms_json") ?: "[]"
                val rooms = try {
                    adapter.fromJson(roomsJson) ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
                _chatRooms.value = rooms
            }
        }
    }

    companion object {
        private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
        val chatRooms = _chatRooms.asStateFlow()
    }
}
