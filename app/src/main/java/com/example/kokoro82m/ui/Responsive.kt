package com.example.kokoro82m.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Returns a text size scaled to the current screen width.
 * A baseline width of 360dp is used for scaling.
 */
@Composable
fun responsiveTextSize(baseSize: TextUnit): TextUnit {
    val configuration = LocalConfiguration.current
    val scale = configuration.screenWidthDp / 360f
    return (baseSize.value * scale).sp
}
