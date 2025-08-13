package com.example.kokoro82m.data

data class Model(
    val id: String,
    val name: String,
    val description: String,
    val repo: String,
    val downloadUrl: String,
    val gated: Boolean,
    var isDownloaded: Boolean = false,
    var hasPartial: Boolean = false,
    val maxTokens: Int? = null,
    val topK: Int? = null,
    val topP: Float? = null,
    val temperature: Float? = null,
) {
    fun getIntConfigValue(key: String, defaultValue: Int): Int = when (key) {
        ConfigKeys.MAX_TOKENS -> maxTokens ?: defaultValue
        ConfigKeys.TOPK -> topK ?: defaultValue
        else -> defaultValue
    }

    fun getFloatConfigValue(key: String, defaultValue: Float): Float = when (key) {
        ConfigKeys.TOPP -> topP ?: defaultValue
        ConfigKeys.TEMPERATURE -> temperature ?: defaultValue
        else -> defaultValue
    }
}

object ConfigKeys {
    const val MAX_TOKENS = "maxTokens"
    const val TOPK = "topK"
    const val TOPP = "topP"
    const val TEMPERATURE = "temperature"
}

const val DEFAULT_MAX_TOKEN = 4096
const val DEFAULT_TOPK = 40
const val DEFAULT_TOPP = 0.95f
const val DEFAULT_TEMPERATURE = 0.8f
