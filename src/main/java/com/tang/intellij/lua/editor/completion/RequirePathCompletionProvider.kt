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

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.impl.ProjectFileIndexFacade
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.lang.type.LuaString

/**
 *
 * Created by tangzx on 2016/12/25.
 */
class RequirePathCompletionProvider : LuaCompletionProvider() {
    override fun addCompletions(session: CompletionSession) {
        val completionParameters = session.parameters
        val completionResultSet = session.resultSet
        val file = completionParameters.originalFile
        val cur = file.findElementAt(completionParameters.offset - 1)
        if (cur != null) {
            val ls = LuaString.getContent(cur.text)
            val content = ls.value.replace('/', PATH_SPLITTER) //统一用.来处理，aaa.bbb.ccc

            val resultSet = completionResultSet.withPrefixMatcher(content)
            addAllFiles(completionParameters, resultSet)
        }

        completionResultSet.stopHere()
    }

    private fun addAllFiles(completionParameters: CompletionParameters, completionResultSet: CompletionResultSet) {
        val project = completionParameters.originalFile.project
        val modules = ModuleManager.getInstance(project).modules
        for (module in modules) {
            val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots
            for (sourceRoot in sourceRoots) {
                addAllFiles(project, completionResultSet, null, sourceRoot.children)
            }
        }
    }

    private fun addAllFiles(project: Project, completionResultSet: CompletionResultSet, pck: String?, children: Array<VirtualFile>) {
        for (child in children) {
            if (!ProjectFileIndexFacade.getInstance(project).isInSource(child))
                continue

            val fileName = FileUtil.getNameWithoutExtension(child.name)
            val newPath = if (pck == null) fileName else "$pck.$fileName"

            if (child.isDirectory) {

                addAllFiles(project, completionResultSet, newPath, child.children)
            } else if (child.fileType === LuaFileType.INSTANCE) {
                val lookupElement = LookupElementBuilder
                        .create(newPath)
                        .withIcon(LuaIcons.FILE)
                        .withInsertHandler(FullPackageInsertHandler())
                completionResultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, 1.0))
            }
        }
    }

    internal class FullPackageInsertHandler : InsertHandler<LookupElement> {

        override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
            val tailOffset = insertionContext.tailOffset
            val cur = insertionContext.file.findElementAt(tailOffset)

            if (cur != null) {
                val start = cur.textOffset

                val ls = LuaString.getContent(cur.text)
                insertionContext.document.deleteString(start + ls.start, start + ls.end)

                val lookupString = lookupElement.lookupString
                insertionContext.document.insertString(start + ls.start, lookupString)
                insertionContext.editor.caretModel.moveToOffset(start + ls.start + lookupString.length)
            }
        }
    }

    companion object {
        private const val PATH_SPLITTER = '.'
    }
}
