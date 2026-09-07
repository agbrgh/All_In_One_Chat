package com.example.all_in_one_chat.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.repository.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var chatRepository: ChatRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationHelper.ACTION_REPLY) {
            val roomId = intent.getStringExtra(NotificationHelper.EXTRA_ROOM_ID) ?: return
            val serviceTypeName = intent.getStringExtra(NotificationHelper.EXTRA_SERVICE_TYPE) ?: return
            val serviceType = ChatServiceType.valueOf(serviceTypeName)
            
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            val replyText = remoteInput?.getCharSequence(NotificationHelper.KEY_TEXT_REPLY)?.toString()

            if (!replyText.isNullOrBlank()) {
                scope.launch {
                    val result = chatRepository.sendMessage(serviceType, roomId, replyText)
                    if (result.isSuccess) {
                        // Update notification to show "Sent" or clear it
                        val notificationManager = NotificationManagerCompat.from(context)
                        notificationManager.cancel(roomId.hashCode())
                    }
                }
            }
        }
    }
}
