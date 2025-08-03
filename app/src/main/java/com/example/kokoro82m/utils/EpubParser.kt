package com.example.kokoro82m.utils

import android.content.Context
import android.net.Uri
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.mediatype.MediaType
import java.io.File

/**
 * Extracts lines of text from an EPUB publication using Readium's streamer.
 */
suspend fun readEpubLines(context: Context, uri: Uri): List<String> {
    // Copy the content URI to a temporary file so that Readium can access it.
    val tmp = File.createTempFile("epub", ".epub", context.cacheDir)
    context.contentResolver.openInputStream(uri)?.use { input ->
        tmp.outputStream().use { output -> input.copyTo(output) }
    } ?: return emptyList()

    val httpClient = DefaultHttpClient()
    val assetRetriever = AssetRetriever(context.contentResolver, httpClient)
    val publicationParser = DefaultPublicationParser(context, assetRetriever, httpClient)
    val opener = PublicationOpener(publicationParser)

    val asset = assetRetriever.retrieve(tmp, MediaType.EPUB).getOrNull() ?: return emptyList()
    val publication = opener.open(asset, allowUserInteraction = false).getOrNull() ?: return emptyList()

    val lines = mutableListOf<String>()
    for (link in publication.readingOrder) {
        val resource = publication.get(link) ?: continue
        val data = resource.read().getOrNull() ?: continue
        val html = data.toString(Charsets.UTF_8)
        val text = html.replace(Regex("<[^>]+>"), " ")
        lines += text.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }
    return lines
}
