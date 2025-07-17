package com.google.ai.edge.gallery.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import com.google.ai.edge.gallery.di.ApplicationScope

@Singleton
class GalleryLifecycleProvider @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope
) {
    // real implementation goes here
}
