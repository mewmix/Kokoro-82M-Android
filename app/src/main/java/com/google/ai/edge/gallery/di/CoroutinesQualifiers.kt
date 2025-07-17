package com.google.ai.edge.gallery.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

@Qualifier
@Retention(BINARY)
annotation class ApplicationScope        // for CoroutineScope

@Qualifier
@Retention(BINARY)
annotation class IoDispatcher            // for Dispatchers.IO
