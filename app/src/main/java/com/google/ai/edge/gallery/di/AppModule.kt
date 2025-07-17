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

package com.google.ai.edge.gallery.di

import com.google.ai.edge.gallery.data.DataStoreRepository
import com.google.ai.edge.gallery.data.DownloadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
  @Provides
  @Singleton
  fun provideDownloadRepository(
    @ApplicationContext context: Context,
    externalScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
  ): DownloadRepository {
    return DownloadRepository(context, externalScope, dispatcher)
  }

  @Provides
  @Singleton
  fun provideExternalScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  @Provides
  @Singleton
  fun provideDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
