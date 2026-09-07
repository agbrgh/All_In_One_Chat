package com.example.all_in_one_chat.data.mapper

import com.example.all_in_one_chat.data.local.entity.ChatMessageEntity
import com.example.all_in_one_chat.data.local.entity.ChatRoomEntity
import com.example.all_in_one_chat.data.local.entity.ChatUserEntity
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.User

fun ChatRoomEntity.toDomain(): ChatRoom {
    return ChatRoom(
        roomId = roomId,
        serviceType = serviceType,
        roomName = roomName,
        unreadCount = unreadCount,
        iconUrl = iconUrl,
        isArchived = isArchived,
        isMuted = isMuted,
        // lastMessage는 Entity의 필드들로 대략적으로 구성하거나, 실제 메시지를 가져와서 연결해야 함
        // 여기서는 간략히 마지막 메시지의 텍스트만 보여주는 용도로 사용하거나 별도 쿼리 필요
        lastMessage = null 
    )
}

fun ChatRoom.toEntity(): ChatRoomEntity {
    return ChatRoomEntity(
        serviceType = serviceType,
        roomId = roomId,
        roomName = roomName,
        lastMessageContent = lastMessage?.content,
        lastMessageTimestamp = lastMessage?.timestamp,
        unreadCount = unreadCount,
        iconUrl = iconUrl,
        isArchived = isArchived,
        isMuted = isMuted
    )
}

fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        messageId = messageId,
        roomId = roomId,
        sender = User(
            id = senderId,
            serviceType = serviceType,
            serviceUserId = senderId, // 간략화를 위해 ID와 동일시
            userName = senderName,
            profileImageUrl = senderImageUrl
        ),
        content = content,
        timestamp = timestamp,
        serviceType = serviceType,
        mediaUrl = mediaUrl,
        mediaLocalPath = mediaLocalPath,
        mediaType = mediaType,
        mediaFileId = mediaFileId,
        isFromMe = isFromMe,
        replyToMessageId = replyToMessageId,
        replyToMessageSender = replyToMessageSender,
        replyToMessageContent = replyToMessageContent
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        serviceType = serviceType,
        messageId = messageId,
        roomId = roomId,
        senderId = sender.id,
        senderName = sender.userName,
        senderImageUrl = sender.profileImageUrl,
        content = content,
        timestamp = timestamp,
        mediaUrl = mediaUrl,
        mediaLocalPath = mediaLocalPath,
        mediaType = mediaType,
        mediaFileId = mediaFileId,
        isFromMe = isFromMe,
        replyToMessageId = replyToMessageId,
        replyToMessageSender = replyToMessageSender,
        replyToMessageContent = replyToMessageContent
    )
}

fun User.toEntity(): ChatUserEntity {
    return ChatUserEntity(
        serviceType = serviceType,
        userId = id,
        name = userName,
        imageUrl = profileImageUrl,
        isMe = false // 기본값, 필요시 외부에서 설정
    )
}

fun ChatUserEntity.toDomain(): User {
    return User(
        id = userId,
        serviceType = serviceType,
        serviceUserId = userId,
        userName = name,
        profileImageUrl = imageUrl
    )
}
