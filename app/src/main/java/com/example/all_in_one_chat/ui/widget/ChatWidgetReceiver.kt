package com.example.all_in_one_chat.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.repository.ChatRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ChatWidget()

    @Inject
    lateinit var chatRepository: ChatRepository

    private val scope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.all_in_one_chat.ACTION_UPDATE_WIDGET") {
            updateData(context)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateData(context)
    }

    fun updateData(context: Context) {
        scope.launch {
            val rooms = chatRepository.getChatRooms().first().take(5)
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val type = Types.newParameterizedType(List::class.java, ChatRoom::class.java)
            val adapter = moshi.adapter<List<ChatRoom>>(type)
            val roomsJson = adapter.toJson(rooms)

            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(ChatWidget::class.java)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[stringPreferencesKey("chat_rooms")] = roomsJson
                    }
                }
                glanceAppWidget.update(context, glanceId)
            }
        }
    }
}
