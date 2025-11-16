package com.studyconnect.app.data.model

data class Message(
    val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)
