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

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.folding.NamedFoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.tang.intellij.lua.psi.LuaTypes

/**
 * Created by tangzx
 * Date : 2015/11/16.
 */
class LuaFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(element: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        return if (quick)
            emptyArray()
        else {
            val list = mutableListOf<FoldingDescriptor>()
            buildLanguageFoldRegions(list, element, document)
            list.toTypedArray()
        }
    }

    private fun buildLanguageFoldRegions(list: MutableList<FoldingDescriptor>, psiElement: PsiElement, document: Document) {
        psiElement.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                val node = element.node
                when(node.elementType) {
                    LuaTypes.BLOCK -> {
                        val prev = node.treePrev//element.prevSibling
                        val next = node.treeNext//element.nextSibling
                        if (prev != null && next != null) {
                            val l = prev.startOffset + prev.textLength
                            val r = next.startOffset

                            val range = TextRange(l, r)
                            if (range.length > 0 && !addOneLineMethodFolding(list, element, range, prev, next)) {
                                list.add(FoldingDescriptor(element, range))
                            }
                        }
                    }
                    LuaTypes.TABLE_EXPR -> {
                        val textRange = node.textRange
                        if (document.getLineNumber(textRange.startOffset) != document.getLineNumber(textRange.endOffset))
                            list.add(FoldingDescriptor(node, textRange))
                    }
                    LuaTypes.BLOCK_COMMENT, LuaTypes.DOC_COMMENT, LuaTypes.STRING -> {
                        val textRange = node.textRange
                        if (document.getLineNumber(textRange.startOffset) != document.getLineNumber(textRange.endOffset))
                            list.add(FoldingDescriptor(node, textRange))
                    }
                }
                super.visitElement(element)
            }
        })
    }

    override fun getPlaceholderText(astNode: ASTNode): String? {
        val type = astNode.elementType
        return when(type) {
            LuaTypes.BLOCK -> HOLDER_TEXT
            LuaTypes.DOC_COMMENT -> "/** ... */"
            LuaTypes.BLOCK_COMMENT -> "--[[ ... ]]"
            LuaTypes.TABLE_EXPR -> "{ ... }"
            LuaTypes.STRING -> "[[ ... ]]"
            else -> null
        }
    }

    private fun addOneLineMethodFolding(descriptors: MutableList<FoldingDescriptor>, block: PsiElement, range: TextRange, prev: ASTNode, next: ASTNode): Boolean {
        val children = block.children
        if (children.isEmpty()) return false

        var validCount = 0
        var first: PsiElement? = null
        for (child in children) {
            if (child !is PsiWhiteSpace) {
                first = child
                validCount++
                if (validCount > 1) return false
            }
        }

        if (validCount == 1) {
            if (first!!.textContains('\n')) return false

            val firstRange = first.textRange
            val lRange = TextRange(range.startOffset, firstRange.startOffset)
            val rRange = TextRange(firstRange.endOffset, range.endOffset)
            if (lRange.isEmpty || rRange.isEmpty)
                return false

            val group = FoldingGroup.newGroup("one-liner")
            descriptors.add(NamedFoldingDescriptor(prev, lRange, group, HOLDER_TEXT))
            descriptors.add(NamedFoldingDescriptor(next, rRange, group, HOLDER_TEXT))

            return true
        }

        return false
    }

    override fun isCollapsedByDefault(astNode: ASTNode) = false

    companion object {

        private const val HOLDER_TEXT = "..."
    }
}
