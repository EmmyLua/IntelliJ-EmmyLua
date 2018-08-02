/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.folding

import com.intellij.lang.folding.CustomFoldingProvider
import com.intellij.openapi.util.text.StringUtil

class LuaFoldingProvider : CustomFoldingProvider() {
    override fun isCustomRegionStart(elementText: String) =
            elementText.contains("{{{") && elementText.matches("--\\s*\\{\\{\\{.*".toRegex())

    override fun isCustomRegionEnd(elementText: String) = elementText.contains("}}}")

    override fun getPlaceholderText(elementText: String): String {
        val textAfterMarker = elementText.replaceFirst("--\\s*\\{\\{\\{(.*)".toRegex(), "$1")
        val result = if (elementText.startsWith("/*")) StringUtil.trimEnd(textAfterMarker, "*/").trim { it <= ' ' } else textAfterMarker.trim { it <= ' ' }
        return if (result.isEmpty()) "..." else result
    }

    override fun getDescription() = "{{{...}}} Comments"

    override fun getStartString() = "{{{ ?"

    override fun getEndString() = "}}}"
}