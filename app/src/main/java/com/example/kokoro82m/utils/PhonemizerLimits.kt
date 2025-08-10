package com.example.kokoro82m.utils

object PhonemizerLimits {
    const val MAX_CHARS_PER_UTTERANCE = 1400  // hard upper bound
    const val TARGET_CHARS_PER_CHUNK = 1100   // packing target to avoid overflows
    const val MIN_CHARS_PER_CHUNK = 200       // don’t make crumbs
}
