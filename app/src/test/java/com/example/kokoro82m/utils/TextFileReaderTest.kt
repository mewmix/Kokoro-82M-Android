package com.example.kokoro82m.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class TextFileReaderTest {
    @Test
    fun readPlainText() {
        val data = "Hello\nWorld"
        val text = TextFileReader.read(ByteArrayInputStream(data.toByteArray()), "text/plain")
        assertEquals(data, text)
    }

    @Test
    fun readEpubHtml() {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("content.html"))
            zos.write("<p>Hello</p>World".toByteArray())
            zos.closeEntry()
        }
        val epubBytes = baos.toByteArray()
        val text = TextFileReader.read(ByteArrayInputStream(epubBytes), "application/epub+zip")
        assertEquals("HelloWorld\n", text)
    }
}

