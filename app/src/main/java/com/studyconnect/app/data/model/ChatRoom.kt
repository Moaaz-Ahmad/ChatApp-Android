package com.studyconnect.app.data.model

data class ChatRoom(
    val id: String = "",
    val title: String = "",
    val memberIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val updatedAt: Long = 0L
)
