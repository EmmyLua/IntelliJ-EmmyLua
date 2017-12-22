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
import com.intellij.icons.AllIcons
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.util.Processor
import com.tang.intellij.lua.editor.LuaNameSuggestionProvider
import com.tang.intellij.lua.stubs.index.LuaClassIndex

class SuggestLocalNameProvider : LuaCompletionProvider() {
    override fun addCompletions(session: CompletionSession) {
        val project = session.parameters.position.project
        LuaClassIndex.processKeys(project, Processor{ className ->
            NameUtil.getSuggestionsByName(className, "", "", false, false, false).forEach {
                val name = LuaNameSuggestionProvider.fixName(it)
                if (session.addWord(name)) {
                    session.resultSet.addElement(LookupElementBuilder.create(name).withIcon(AllIcons.Actions.RefactoringBulb))
                }
            }
            true
        })
    }
}