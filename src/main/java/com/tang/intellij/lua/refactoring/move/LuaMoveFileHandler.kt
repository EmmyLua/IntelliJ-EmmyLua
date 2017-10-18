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

package com.tang.intellij.lua.refactoring.move

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFileHandler
import com.intellij.usageView.UsageInfo
import com.tang.intellij.lua.psi.LuaPsiFile
import com.tang.intellij.lua.psi.LuaFileUtil
import com.tang.intellij.lua.reference.LuaRequireReference
import java.util.*

class LuaMoveFileHandler : MoveFileHandler() {
    companion object {
        private val REFERENCED_ELEMENT = Key.create<PsiNamedElement>("LUA_REFERENCED_ELEMENT")
    }

    override fun updateMovedFile(file: PsiFile) {

    }

    override fun prepareMovedFile(file: PsiFile, moveDestination: PsiDirectory, oldToNewMap: MutableMap<PsiElement, PsiElement>?) {

    }

    override fun findUsages(file: PsiFile, newParent: PsiDirectory, searchInComments: Boolean, searchInNonJavaFiles: Boolean): MutableList<UsageInfo> {
        val usages = mutableListOf<UsageInfo>()
        val handler = object : FindUsagesHandler(file){}
        val elementsToProcess = ArrayList<PsiElement>()
        Collections.addAll(elementsToProcess, *handler.primaryElements)
        Collections.addAll(elementsToProcess, *handler.secondaryElements)
        for (e in elementsToProcess) {
            handler.processElementUsages(e, { usageInfo ->
                if (!usageInfo.isNonCodeUsage) {
                    usageInfo.element?.putCopyableUserData(REFERENCED_ELEMENT, file)
                    usages.add(usageInfo)
                }
                true
            }, FindUsagesHandler.createFindUsagesOptions(file.project, null))
        }
        return usages
    }

    override fun retargetUsages(usageInfos: MutableList<UsageInfo>, oldToNewMap: MutableMap<PsiElement, PsiElement>) {
        for (usageInfo in usageInfos) {
            val reference = usageInfo.reference
            if (reference is LuaRequireReference) {
                val element = usageInfo.element!!
                val file = element.getCopyableUserData(REFERENCED_ELEMENT) as PsiFile
                element.putCopyableUserData(REFERENCED_ELEMENT, null)

                val requirePath = LuaFileUtil.asRequirePath(file.project, file.virtualFile)
                requirePath?.let {
                    reference.setPath(requirePath)
                }
            }
        }
    }

    override fun canProcessElement(file: PsiFile): Boolean {
        return file is LuaPsiFile
    }
}