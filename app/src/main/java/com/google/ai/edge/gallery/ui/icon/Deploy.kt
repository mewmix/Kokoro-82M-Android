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

package com.google.ai.edge.gallery.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Filled.Deploy: ImageVector
  get() {
    if (_deploy != null) {
      return _deploy!!
    }
    _deploy = materialIcon(name = "Filled.Deploy") {
      materialPath {
        moveTo(12.0f, 2.0f)
        lineTo(2.0f, 7.0f)
        lineTo(12.0f, 12.0f)
        lineTo(22.0f, 7.0f)
        lineTo(12.0f, 2.0f)
        close()
        moveTo(2.0f, 17.0f)
        lineTo(12.0f, 22.0f)
        lineTo(22.0f, 17.0f)
        lineTo(12.0f, 12.0f)
        lineTo(2.0f, 17.0f)
        close()
      }
    }
    return _deploy!!
  }

private var _deploy: ImageVector? = null
