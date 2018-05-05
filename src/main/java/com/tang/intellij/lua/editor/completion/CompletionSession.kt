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

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.Key
import java.util.*

/**
 *
 * Created by TangZX on 2017/5/22.
 */
class CompletionSession(val parameters: CompletionParameters, val resultSet: CompletionResultSet) {
    var isSuggestWords = true

    private val words = HashSet<String>()

    fun addWord(word: String): Boolean {
        return words.add(word)
    }

    companion object {

        val KEY = Key.create<CompletionSession>("lua.CompletionSession")

        operator fun get(completionParameters: CompletionParameters): CompletionSession {
            return completionParameters.editor.getUserData(KEY)!!
        }
    }
}
