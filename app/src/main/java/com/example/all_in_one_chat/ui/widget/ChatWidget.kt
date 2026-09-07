package com.example.all_in_one_chat.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.GlanceTheme
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.all_in_one_chat.MainActivity
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class ChatWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, ChatRoom::class.java)
        val adapter = moshi.adapter<List<ChatRoom>>(type)

        provideContent {
            val prefs = currentState<Preferences>()
            val roomsJson = prefs[stringPreferencesKey("chat_rooms")] ?: "[]"
            val rooms: List<ChatRoom> = try {
                adapter.fromJson(roomsJson) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            GlanceTheme {
                ChatWidgetContent(rooms)
            }
        }
    }

    @Composable
    private fun ChatWidgetContent(rooms: List<ChatRoom>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
        ) {
            Text(
                text = "올인원챗 주요 대화",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onBackground
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            if (rooms.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "대화 목록이 없습니다.",
                        style = TextStyle(color = GlanceTheme.colors.onBackground)
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(rooms) { room ->
                        ChatRoomItem(room)
                    }
                }
            }
        }
    }

    @Composable
    private fun ChatRoomItem(room: ChatRoom) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = room.roomName,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                Text(
                    text = room.lastMessage?.content ?: "",
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }
}
