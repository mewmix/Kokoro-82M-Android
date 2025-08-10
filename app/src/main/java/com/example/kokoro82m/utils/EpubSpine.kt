package com.example.kokoro82m.utils

import android.content.Context
import android.net.Uri
import java.io.BufferedInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

object EpubSpine {
    data class Spine(val rootfilePath: String, val hrefsInOrder: List<String>)
    data class PackageInfo(val opfDir: String, val manifest: Map<String, String>, val spineIds: List<String>)

    fun open(ctx: Context, uri: Uri): Spine {
        val containerXml = ctx.contentResolver.openInputStream(uri)?.use { base ->
            ZipInputStream(BufferedInputStream(base)).use { zis ->
                readEntryBytes(zis, "META-INF/container.xml")
            }
        } ?: return fallback()
        val containerDoc = Jsoup.parse(containerXml, "", Parser.xmlParser())
        val rootfilePath = containerDoc.selectFirst("rootfile")?.attr("full-path") ?: return fallback()

        val opfBytes = ctx.contentResolver.openInputStream(uri)?.use { base ->
            ZipInputStream(BufferedInputStream(base)).use { zis ->
                readEntryBytes(zis, rootfilePath)
            }
        } ?: return fallback()
        val opfDoc = Jsoup.parse(opfBytes, "", Parser.xmlParser())
        val opfDir = rootfilePath.substringBeforeLast('/', "")
        val manifest = opfDoc.select("manifest > item").associate {
            it.attr("id") to it.attr("href")
        }
        val spineIds = opfDoc.select("spine > itemref").map { it.attr("idref") }
        val hrefsInOrder = spineIds.mapNotNull { manifest[it] }.map { href ->
            if (opfDir.isEmpty()) href else "$opfDir/$href"
        }
        return Spine(rootfilePath, hrefsInOrder)
    }

    private fun readEntryBytes(zis: ZipInputStream, targetName: String): ByteArray? {
        var e: ZipEntry? = zis.nextEntry
        while (e != null) {
            if (!e.isDirectory && e.name == targetName) {
                val bytes = zis.readBytes()
                zis.closeEntry()
                return bytes
            }
            zis.closeEntry()
            e = zis.nextEntry
        }
        return null
    }

    private fun resetToStart(zis: ZipInputStream) {
        // ZipInputStream has no seek; we reopen per caller once, so we cache nothing here.
        // Caller (open) owns single pass for container/opf; readEntryBytes is called twice before we iterate content.
    }

    private fun fallback(): Spine = Spine(rootfilePath = "", hrefsInOrder = emptyList())
}
