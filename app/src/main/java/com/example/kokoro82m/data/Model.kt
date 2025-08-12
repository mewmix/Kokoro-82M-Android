package com.example.kokoro82m.data

data class Model(
    val id: String,
    val name: String,
    val description: String,
    val repo: String,
    val downloadUrl: String,
    val gated: Boolean,
    /** Optional absolute path if the model lives outside app storage */
    var localPath: String? = null,
    var isDownloaded: Boolean = false,
    var hasPartial: Boolean = false,
)
