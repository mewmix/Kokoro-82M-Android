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

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.ai.edge.gallery.data.HUGGING_FACE_CLIENT_ID
import com.google.ai.edge.gallery.data.HUGGING_FACE_REDIRECT_URL
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

fun authorizeWithHuggingFace(
  context: Context,
  authService: AuthorizationService,
  launcher: ActivityResultLauncher<Intent>,
) {
  val serviceConfig =
    AuthorizationServiceConfiguration(
      /* authorizationEndpoint = */ "https://huggingface.co/oauth/authorize",
      /* tokenEndpoint = */ "https://huggingface.co/oauth/token",
    )

  val authRequest =
    AuthorizationRequest.Builder(
        serviceConfig,
        HUGGING_FACE_CLIENT_ID,
        ResponseTypeValues.CODE,
        HUGGING_FACE_REDIRECT_URL,
      )
      .setScopes("profile")
      .build()

  val authIntent = authService.getAuthorizationRequestIntent(authRequest)
  launcher.launch(authIntent)
}
