/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun ClickableLink(text: String, url: String) {
  val context = LocalContext.current
  val annotatedString = buildAnnotatedString {
    append(text)
    addStyle(
      style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
      start = 0,
      end = text.length,
    )
    addStringAnnotation(tag = "URL", annotation = url, start = 0, end = text.length)
  }

  ClickableText(
    text = annotatedString,
    onClick = {
      annotatedString.getStringAnnotations("URL", it, it).firstOrNull()?.let { annotation ->
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
        context.startActivity(intent)
      }
    },
  )
}
