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

package com.tang.intellij.lua.codeInsight.highlighting

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Consumer
import com.tang.intellij.lua.psi.*

/**
 *
 * Created by tangzx on 2017/3/18.
 */
class LuaHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactoryBase() {

    override fun createHighlightUsagesHandler(editor: Editor,
                                              psiFile: PsiFile,
                                              psiElement: PsiElement): HighlightUsagesHandlerBase<*>? {
        when(psiElement.node.elementType) {
            LuaTypes.RETURN -> {
                val returnStat = PsiTreeUtil.getParentOfType(psiElement, LuaReturnStat::class.java)
                if (returnStat != null) {
                    val funcBody = PsiTreeUtil.getParentOfType(returnStat, LuaFuncBody::class.java)
                    if (funcBody != null) {
                        return LuaHighlightExitPointsHandler(editor, psiFile, returnStat, funcBody)
                    }
                }
            }

            LuaTypes.BREAK -> {
                val loop = PsiTreeUtil.getParentOfType(psiElement, LuaLoop::class.java)
                if (loop != null)
                    return LoopHandler(editor, psiFile, psiElement, loop)
            }

            else -> {
                val parent = psiElement.parent
                val parentType = parent.node.elementType
                if (parentType == LuaTypes.BINARY_OP || parentType == LuaTypes.UNARY_OP) {
                    return object : HighlightUsagesHandlerBase<PsiElement>(editor, psiFile) {
                        override fun selectTargets(list: MutableList<PsiElement>?, consumer: Consumer<MutableList<PsiElement>>?) { }

                        override fun computeUsages(list: MutableList<PsiElement>) {
                            addOccurrence(parent.parent)
                        }

                        override fun getTargets() = arrayListOf(psiElement)
                    }
                }
            }
        }
        return null
    }
}

private class LoopHandler(editor: Editor, psiFile: PsiFile, val psi:PsiElement, val loop: LuaLoop) : HighlightUsagesHandlerBase<PsiElement>(editor, psiFile) {
    override fun getTargets() = arrayListOf(psi)

    override fun computeUsages(list: MutableList<PsiElement>?) {
        loop.head?.let { addOccurrence(it) }
        loop.end?.let { addOccurrence(it) }
        addOccurrence(psi)
    }

    override fun selectTargets(list: MutableList<PsiElement>?, consumer: Consumer<MutableList<PsiElement>>?) {}

}