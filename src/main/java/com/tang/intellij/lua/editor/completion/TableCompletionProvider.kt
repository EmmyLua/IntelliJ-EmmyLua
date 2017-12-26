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

package com.tang.intellij.lua.editor.completion

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.tang.intellij.lua.lang.LuaIcons

class TableCompletionProvider : LuaCompletionProvider() {

    companion object {
        private val metaMethodNames = mapOf(
                "__add" to "a + b",
                "__sub" to "a - b",
                "__mul" to "a * b",
                "__div" to "a / b",
                "__mod" to "a % b",
                "__pow" to "a ^ b",
                "__unm" to "-a",
                "__concat" to "a .. b",
                "__len" to "#a",
                "__eq" to "a == a",
                "__lt" to "a < b",
                "__le" to "a <= b",
                "__index" to "Meta method",
                "__newindex" to "Meta method",
                "__call" to "Meta method",
                "__tostring" to "Meta method",
                "__metatable" to "Meta method"
        )
    }

    override fun addCompletions(session: CompletionSession) {
        val completionResultSet = session.resultSet

        metaMethodNames.forEach {
            val b = LookupElementBuilder.create(it.key)
                    .withTypeText(it.value)
                    .withIcon(LuaIcons.META_METHOD)
            completionResultSet.addElement(b)
        }
    }
}