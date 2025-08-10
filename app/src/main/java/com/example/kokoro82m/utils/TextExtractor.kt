package com.example.kokoro82m.utils

import android.content.Context
import android.net.Uri
import org.jsoup.Jsoup
import java.io.BufferedInputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object TextExtractor {
    data class DocMeta(val title: String = "EPUB")

    fun extractEpubAsChunks(
        ctx: Context,
        uri: Uri,
        maxChars: Int = PhonemizerLimits.MAX_CHARS_PER_UTTERANCE,
        targetChars: Int = PhonemizerLimits.TARGET_CHARS_PER_CHUNK
    ): Pair<Sequence<String>, DocMeta> {
        val (spineSeq, meta) = readSpineTexts(ctx, uri)
        val sentenceSeq = sequence<String> {
            for (text in spineSeq) {
                normalize(text).let { t ->
                    splitIntoSentences(t).forEach { yield(it) }
                }
            }
        }
        return packSentences(sentenceSeq, targetChars, maxChars) to meta
    }

    // --- internals ---

    private fun readSpineTexts(ctx: Context, uri: Uri): Pair<Sequence<String>, DocMeta> {
        val spine = EpubSpine.open(ctx, uri)
        val seq = if (spine.hrefsInOrder.isEmpty()) {
            // Fallback: all xhtml/html by name order
            readAllHtmlEntries(ctx, uri)
        } else {
            readHtmlEntriesInOrder(ctx, uri, spine.hrefsInOrder)
        }
        return seq to DocMeta()
    }

    private fun readAllHtmlEntries(ctx: Context, uri: Uri): Sequence<String> = sequence {
        ctx.contentResolver.openInputStream(uri).use { base ->
            ZipInputStream(BufferedInputStream(base)).use { zis ->
                var e: ZipEntry? = zis.nextEntry
                val items = mutableListOf<Pair<String, String>>()
                while (e != null) {
                    val n = e.name.lowercase()
                    if (!e.isDirectory && (n.endsWith(".xhtml") || n.endsWith(".html") || n.endsWith(".htm"))) {
                        items += n to extractTextFromHtmlBytes(zis.readBytes())
                    }
                    zis.closeEntry()
                    e = zis.nextEntry
                }
                items.sortedBy { it.first }.forEach { yield(it.second) }
            }
        }
    }

    private fun readHtmlEntriesInOrder(ctx: Context, uri: Uri, orderedHrefs: List<String>): Sequence<String> = sequence {
        // One pass per href to avoid keeping whole book in RAM
        for (href in orderedHrefs) {
            ctx.contentResolver.openInputStream(uri).use { base ->
                ZipInputStream(BufferedInputStream(base)).use { zis ->
                    var e: ZipEntry? = zis.nextEntry
                    while (e != null) {
                        if (!e.isDirectory && e.name == href) {
                            yield(extractTextFromHtmlBytes(zis.readBytes()))
                            zis.closeEntry()
                            break
                        }
                        zis.closeEntry()
                        e = zis.nextEntry
                    }
                }
            }
        }
    }

    private fun extractTextFromHtmlBytes(bytes: ByteArray, cs: Charset = Charsets.UTF_8): String {
        // Jsoup handles XHTML; strip scripts/styles automatically
        return Jsoup.parse(bytes.toString(cs)).text()
    }

    private fun normalize(s: String): String = buildString(s.length) {
        var lastWasSpace = false
        for (ch in s) {
            val c = when (ch) {
                '\u00A0', '\u2007', '\u202F' -> ' '
                else -> ch
            }
            val isSpace = c.isWhitespace()
            if (isSpace) {
                if (!lastWasSpace) append(' ')
            } else append(c)
            lastWasSpace = isSpace
        }
        // basic trims
    }.trim()

    private fun splitIntoSentences(text: String): List<String> {
        // Lightweight sentence split that respects common boundaries; fast + no heavy NLP
        // We’ll split on . ! ? followed by space/newline and capital or quote.
        val regex = Regex("(?<=[.!?])\\s+(?=[\\p{Lu}\"'\\(\\[])")
        return text.split(regex).filter { it.isNotBlank() }
    }

    private fun packSentences(
        sentences: Sequence<String>,
        targetChars: Int,
        maxChars: Int
    ): Sequence<String> = sequence {
        val buf = StringBuilder()
        suspend fun SequenceScope<String>.flush() {
            if (buf.isNotEmpty()) {
                yield(buf.toString().trim())
                buf.clear()
            }
        }
        for (s in sentences) {
            val needed = if (buf.isEmpty()) s.length else s.length + 1 // space
            if (buf.length + needed <= targetChars) {
                if (buf.isNotEmpty()) buf.append(' ')
                buf.append(s)
            } else if (s.length <= maxChars) {
                flush()
                buf.append(s)
                flush()
            } else {
                // sentence itself exceeds max: hard-wrap
                var i = 0
                while (i < s.length) {
                    val end = minOf(i + maxChars, s.length)
                    yield(s.substring(i, end))
                    i = end
                }
            }
            if (buf.length >= targetChars) flush()
        }
        flush()
    }
}
