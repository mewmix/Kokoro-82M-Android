package com.example.kokoro82m.utils

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.util.zip.ZipInputStream

object TextFileReader {
    fun read(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri) ?: ""
        val stream = context.contentResolver.openInputStream(uri)
        return stream?.use { read(it, mime) } ?: ""
    }

    fun read(stream: InputStream, mime: String): String {
        return when {
            mime == "application/epub+zip" -> readEpub(stream)
            mime == "text/html" || mime == "application/xhtml+xml" -> {
                val html = stream.bufferedReader().use { it.readText() }
                stripHtml(html)
            }
            else -> stream.bufferedReader().use { it.readText() }
        }
    }

    private fun readEpub(stream: InputStream): String {
        val sb = StringBuilder()
        ZipInputStream(stream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && (entry.name.endsWith(".xhtml") || entry.name.endsWith(".html"))) {
                    val html = zis.bufferedReader().use { it.readText() }
                    sb.append(stripHtml(html)).append('\n')
                }
                entry = zis.nextEntry
            }
        }
        return sb.toString()
    }

    private fun stripHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "")
    }
}

