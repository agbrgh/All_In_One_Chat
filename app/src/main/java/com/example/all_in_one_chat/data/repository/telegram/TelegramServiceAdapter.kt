package com.example.all_in_one_chat.data.repository.telegram

import android.content.Context
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.repository.ChatServiceAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi.AuthorizationState
import org.drinkless.tdlib.TdApi.AuthorizationStateClosed
import org.drinkless.tdlib.TdApi.AuthorizationStateClosing
import org.drinkless.tdlib.TdApi.AuthorizationStateLoggingOut
import org.drinkless.tdlib.TdApi.AuthorizationStateReady
import org.drinkless.tdlib.TdApi.AuthorizationStateWaitCode
import org.drinkless.tdlib.TdApi.AuthorizationStateWaitPassword
import org.drinkless.tdlib.TdApi.AuthorizationStateWaitPhoneNumber
import org.drinkless.tdlib.TdApi.AuthorizationStateWaitTdlibParameters
import org.drinkless.tdlib.TdApi.Chat
import org.drinkless.tdlib.TdApi.ChatListMain
import org.drinkless.tdlib.TdApi.Chats
import org.drinkless.tdlib.TdApi.CheckAuthenticationCode
import org.drinkless.tdlib.TdApi.CheckAuthenticationPassword
import org.drinkless.tdlib.TdApi.DownloadFile
import org.drinkless.tdlib.TdApi.Error
import org.drinkless.tdlib.TdApi.FormattedText
import org.drinkless.tdlib.TdApi.GetChat
import org.drinkless.tdlib.TdApi.GetChatHistory
import org.drinkless.tdlib.TdApi.GetChats
import org.drinkless.tdlib.TdApi.InputMessageReplyToMessage
import org.drinkless.tdlib.TdApi.InputMessageText
import org.drinkless.tdlib.TdApi.Message
import org.drinkless.tdlib.TdApi.MessagePhoto
import org.drinkless.tdlib.TdApi.MessageSenderChat
import org.drinkless.tdlib.TdApi.MessageSenderUser
import org.drinkless.tdlib.TdApi.MessageSourceChatHistory
import org.drinkless.tdlib.TdApi.MessageText
import org.drinkless.tdlib.TdApi.MessageVideo
import org.drinkless.tdlib.TdApi.Messages
import org.drinkless.tdlib.TdApi.DeleteMessages
import org.drinkless.tdlib.TdApi.ForwardMessages
import org.drinkless.tdlib.TdApi.Object
import org.drinkless.tdlib.TdApi.OpenChat
import org.drinkless.tdlib.TdApi.SendMessage
import org.drinkless.tdlib.TdApi.SetAuthenticationPhoneNumber
import org.drinkless.tdlib.TdApi.SetTdlibParameters
import org.drinkless.tdlib.TdApi.UpdateAuthorizationState
import org.drinkless.tdlib.TdApi.UpdateFile
import org.drinkless.tdlib.TdApi.UpdateNewMessage
import org.drinkless.tdlib.TdApi.ViewMessages
import org.drinkless.tdlib.TdApi.MessageAudio
import org.drinkless.tdlib.TdApi.MessageDocument
import org.drinkless.tdlib.TdApi.MessageSticker
import org.drinkless.tdlib.TdApi.GetUser
import org.drinkless.tdlib.TdApi.User as TdUser
import com.example.all_in_one_chat.domain.model.MediaType
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TelegramServiceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
) : ChatServiceAdapter {
    override val serviceType: ChatServiceType = ChatServiceType.TELEGRAM
    
    override val isConnected: Flow<Boolean> by lazy {
        _authState.map { it is TelegramAuthState.Authenticated }
    }

    private var client: Client? = null
    private val _authState = MutableStateFlow<TelegramAuthState>(TelegramAuthState.Initial)
    val authState: StateFlow<TelegramAuthState> = _authState.asStateFlow()

    private val _newMessages = MutableSharedFlow<ChatMessage>()
    private val _fileUpdates = MutableSharedFlow<Pair<Int, String>>()
    override fun observeFileUpdates(): Flow<Pair<Int, String>> = _fileUpdates.asSharedFlow()

    private val userCache = ConcurrentHashMap<Long, String>()

    companion object {
        init {
            try {
                // AAR 내부의 네이티브 라이브러리를 로드합니다.
                System.loadLibrary("tdjni")
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }
    }

    init {
        setupClient()
    }

    private fun setupClient() {
        client = Client.create({ update ->
            handleUpdate(update)
        }, null, null)
    }

    private fun handleUpdate(update: Object) {
        when (update) {
            is UpdateAuthorizationState -> {
                handleAuthState(update.authorizationState)
            }
            is UpdateNewMessage -> {
                // Map TdApi.Message to domain ChatMessage and emit
                val domainMessage = mapToDomainMessage(update.message)
                _newMessages.tryEmit(domainMessage)
                
                // If user name is missing in cache, fetch it for future messages
                update.message.senderId.let { sender ->
                    if (sender is MessageSenderUser) {
                        fetchAndCacheUser(sender.userId)
                    }
                }
            }
            is UpdateFile -> {
                val file = update.file
                val localPath = file.local.path
                if (localPath.isNotEmpty()) {
                    _fileUpdates.tryEmit(file.id to localPath)
                }
            }
        }
    }

    private fun fetchAndCacheUser(userId: Long) {
        if (userCache.containsKey(userId)) return
        client?.send(GetUser(userId)) { result ->
            if (result is TdUser) {
                val name = "${result.firstName} ${result.lastName}".trim()
                userCache[userId] = name.ifEmpty { "User $userId" }
            }
        }
    }

    private fun handleAuthState(state: AuthorizationState) {
        when (state) {
            is AuthorizationStateWaitTdlibParameters -> {
                val parameters = SetTdlibParameters().apply {
                    databaseDirectory = File(context.filesDir, "tdlib").absolutePath
                    useMessageDatabase = true
                    useSecretChats = true
                    apiId = TelegramConfig.API_ID
                    apiHash = TelegramConfig.API_HASH
                    systemLanguageCode = "en"
                    deviceModel = "Android"
                    systemVersion = "1.0"
                    applicationVersion = "1.0"
                    // Add missing fields required by this version of TdApi
                    useTestDc = false
                    useFileDatabase = true
                    useChatInfoDatabase = true
                }
                client?.send(parameters) { }
            }
            is AuthorizationStateWaitPhoneNumber -> {
                _authState.value = TelegramAuthState.WaitPhoneNumber
            }
            is AuthorizationStateWaitCode -> {
                _authState.value = TelegramAuthState.WaitCode
            }
            is AuthorizationStateWaitPassword -> {
                _authState.value = TelegramAuthState.WaitPassword
            }
            is AuthorizationStateReady -> {
                _authState.value = TelegramAuthState.Authenticated
            }
            is AuthorizationStateLoggingOut -> {
                _authState.value = TelegramAuthState.Initial
            }
            is AuthorizationStateClosing -> {}
            is AuthorizationStateClosed -> {
                setupClient()
            }
        }
    }

    override suspend fun login(): Result<Unit> {
        if (client == null) setupClient()
        return Result.success(Unit)
    }

    fun setPhoneNumber(phoneNumber: String) {
        client?.send(SetAuthenticationPhoneNumber(phoneNumber, null)) { result ->
            if (result is Error) {
                FirebaseCrashlytics.getInstance().recordException(Exception("Telegram Auth Error: ${result.message}"))
                _authState.value = TelegramAuthState.Error(result.message)
            }
        }
    }

    fun setCode(code: String) {
        client?.send(CheckAuthenticationCode(code)) { result ->
            if (result is Error) {
                _authState.value = TelegramAuthState.Error(result.message)
            }
        }
    }

    fun setPassword(password: String) {
        client?.send(CheckAuthenticationPassword(password)) { result ->
            if (result is Error) {
                _authState.value = TelegramAuthState.Error(result.message)
            }
        }
    }

    override suspend fun getChatRooms(): Result<List<ChatRoom>> =
        suspendCancellableCoroutine { continuation ->
            client?.send(GetChats(ChatListMain(), 100)) { result ->
                when (result) {
                    is Chats -> {
                        val chatIds = result.chatIds
                        if (chatIds.isEmpty()) {
                            continuation.resume(Result.success(emptyList()))
                            return@send
                        }

                        val chatRooms = mutableListOf<ChatRoom>()
                        var completedCount = 0

                        for (chatId in chatIds) {
                            client?.send(GetChat(chatId)) { chatResult ->
                                synchronized(chatRooms) {
                                    if (chatResult is Chat) {
                                        chatRooms.add(mapToDomainChatRoom(chatResult))
                                    }
                                    completedCount++
                                    if (completedCount == chatIds.size) {
                                        if (continuation.isActive) {
                                            continuation.resume(Result.success(chatRooms))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is Error -> {
                        FirebaseCrashlytics.getInstance().recordException(Exception("Telegram GetChatRooms Error: ${result.message}"))
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception(result.message)))
                        }
                    }

                    else -> {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception("Unknown result type")))
                        }
                    }
                }
            }
        }

    private fun mapToDomainChatRoom(chat: Chat): ChatRoom {
        return ChatRoom(
            roomId = chat.id.toString(),
            serviceType = ChatServiceType.TELEGRAM,
            roomName = chat.title,
            lastMessage = chat.lastMessage?.let { mapToDomainMessage(it) },
            unreadCount = chat.unreadCount
        )
    }

    override suspend fun getMessages(roomId: String): Result<List<ChatMessage>> =
        suspendCancellableCoroutine { continuation ->
            client?.send(GetChatHistory(roomId.toLong(), 0, 0, 50, false)) { result ->
                when (result) {
                    is Messages -> {
                        val domainMessages = result.messages.map { mapToDomainMessage(it) }
                        continuation.resume(Result.success(domainMessages))
                    }

                    is Error -> {
                        continuation.resume(Result.failure(Exception(result.message)))
                    }

                    else -> {
                        continuation.resume(Result.failure(Exception("Unknown result type")))
                    }
                }
            }
        }

    override suspend fun sendMessage(roomId: String, content: String, replyToMessageId: String?): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val contentObj = InputMessageText(FormattedText(content, null), null, true)
            val replyTo = replyToMessageId?.toLong()?.let { InputMessageReplyToMessage(it, null, 0, null) }
            
            client?.send(
                SendMessage(
                    roomId.toLong(),
                    null,
                    replyTo,
                    null,
                    null,
                    contentObj
                )
            ) { result ->
                if (result is Error) {
                    continuation.resume(Result.failure(Exception(result.message)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }

    override suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            client?.send(DeleteMessages(roomId.toLong(), longArrayOf(messageId.toLong()), true)) { result ->
                if (result is Error) {
                    continuation.resume(Result.failure(Exception(result.message)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }

    override suspend fun forwardMessage(fromRoomId: String, messageId: String, toRoomId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            client?.send(
                ForwardMessages(
                    toRoomId.toLong(),
                    null,
                    fromRoomId.toLong(),
                    longArrayOf(messageId.toLong()),
                    null,
                    false,
                    false
                )
            ) { result ->
                if (result is Error) {
                    continuation.resume(Result.failure(Exception(result.message)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }

    override fun observeNewMessages(): Flow<ChatMessage> = _newMessages.asSharedFlow()

    override suspend fun markAsRead(roomId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val chatId = roomId.toLong()
            // 안정적인 읽음 처리를 위해 먼저 openChat 호출
            client?.send(OpenChat(chatId)) { openResult ->
                if (openResult is Error) {
                    continuation.resume(Result.failure(Exception(openResult.message)))
                    return@send
                }

                client?.send(GetChat(chatId)) { chatResult ->
                    if (chatResult is Chat) {
                        val lastMessageId = chatResult.lastMessage?.id ?: 0L
                        if (lastMessageId != 0L) {
                            // 마지막 메시지를 기준으로 viewMessages 호출하여 읽음 처리
                            client?.send(ViewMessages(chatId, longArrayOf(lastMessageId), MessageSourceChatHistory(), true)) { viewResult ->
                                if (viewResult is Error) {
                                    continuation.resume(Result.failure(Exception(viewResult.message)))
                                } else {
                                    continuation.resume(Result.success(Unit))
                                }
                            }
                        } else {
                            continuation.resume(Result.success(Unit))
                        }
                    } else if (chatResult is Error) {
                        continuation.resume(Result.failure(Exception(chatResult.message)))
                    } else {
                        continuation.resume(Result.failure(Exception("Unknown error fetching chat")))
                    }
                }
            }
        }

    override suspend fun downloadMedia(messageId: String, fileId: Int): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            client?.send(DownloadFile(fileId, 1, 0, 0, true)) { result ->
                if (result is Error) {
                    continuation.resume(Result.failure(Exception(result.message)))
                } else {
                    continuation.resume(Result.success(Unit))
                }
            }
        }

    private fun mapToDomainMessage(message: Message): ChatMessage {
        val mediaUrl: String? = null
        var mediaLocalPath: String? = null
        var mediaType: MediaType? = null
        var mediaFileId: Int? = null

        val content = when (val msgContent = message.content) {
            is MessageText -> msgContent.text.text
            is MessagePhoto -> {
                mediaType = MediaType.IMAGE
                val photo = msgContent.photo.sizes.lastOrNull()
                mediaFileId = photo?.photo?.id
                mediaLocalPath = photo?.photo?.local?.path?.takeIf { it.isNotEmpty() }
                
                if ((mediaLocalPath == null) && (mediaFileId != null)) {
                    // Photos should be downloaded immediately like text
                    client?.send(DownloadFile(mediaFileId, 1, 0, 0, true)) { }
                }
                "[이미지]"
            }
            is MessageVideo -> {
                mediaType = MediaType.VIDEO
                mediaFileId = msgContent.video.video.id
                mediaLocalPath = msgContent.video.video.local.path.takeIf { it.isNotEmpty() }
                "동영상이 전송되었습니다"
            }
            is MessageAudio -> {
                mediaType = MediaType.AUDIO
                mediaFileId = msgContent.audio.audio.id
                mediaLocalPath = msgContent.audio.audio.local.path.takeIf { it.isNotEmpty() }
                "[음성 메시지]"
            }
            is MessageDocument -> {
                mediaType = MediaType.FILE
                mediaFileId = msgContent.document.document.id
                mediaLocalPath = msgContent.document.document.local.path.takeIf { it.isNotEmpty() }
                "[문서: ${msgContent.document.fileName}]"
            }
            is MessageSticker -> {
                mediaType = MediaType.IMAGE
                mediaFileId = msgContent.sticker.sticker.id
                mediaLocalPath = msgContent.sticker.sticker.local.path.takeIf { it.isNotEmpty() }
                "[스티커]"
            }
            else -> "[지원하지 않는 메시지 타입]"
        }

        val senderId = when (val sender = message.senderId) {
            is MessageSenderUser -> sender.userId.toString()
            is MessageSenderChat -> sender.chatId.toString()
            else -> "unknown"
        }
        
        val senderName = if (message.senderId is MessageSenderUser) {
            val userId = (message.senderId as MessageSenderUser).userId
            userCache[userId] ?: "User $senderId"
        } else {
            "User $senderId"
        }

        return ChatMessage(
            messageId = message.id.toString(),
            roomId = message.chatId.toString(),
            sender = com.example.all_in_one_chat.domain.model.User(
                id = senderId,
                serviceType = ChatServiceType.TELEGRAM,
                serviceUserId = senderId,
                userName = senderName
            ),
            content = content,
            timestamp = message.date.toLong() * 1000,
            serviceType = ChatServiceType.TELEGRAM,
            mediaUrl = mediaUrl,
            mediaLocalPath = mediaLocalPath,
            mediaType = mediaType,
            mediaFileId = mediaFileId,
            isFromMe = message.isOutgoing
        )
    }
}
