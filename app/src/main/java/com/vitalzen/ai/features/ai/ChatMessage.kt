package com.vitalzen.ai.features.ai

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)
