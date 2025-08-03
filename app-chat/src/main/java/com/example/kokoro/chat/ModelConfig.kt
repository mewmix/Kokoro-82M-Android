package com.example.kokoro.chat

data class ModelConfig(
    val temperature: Float = 0.8f,
    val topK: Int = 40,
    val topP: Float = 0.9f,
    val maxTokens: Int = 1024
)
