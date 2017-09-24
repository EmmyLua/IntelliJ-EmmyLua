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
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.folding.NamedFoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.LuaBlock
import com.tang.intellij.lua.psi.LuaTypes

/**
 * Created by tangzx
 * Date : 2015/11/16.
 */
class LuaFoldingBuilder : CustomFoldingBuilder(), FoldingBuilder {

    override fun buildLanguageFoldRegions(list: MutableList<FoldingDescriptor>, psiElement: PsiElement, document: Document, b: Boolean) {
        collectDescriptorsRecursively(psiElement.node, document, list)
    }

    override fun getLanguagePlaceholderText(astNode: ASTNode, textRange: TextRange): String? {
        val type = astNode.elementType
        return when(type) {
            LuaTypes.BLOCK -> HOLDER_TEXT
            LuaTypes.DOC_COMMENT -> "/** ... */"
            LuaTypes.TABLE_EXPR -> "{ ... }"
            else -> null
        }
    }

    private fun collectDescriptorsRecursively(node: ASTNode,
                                              document: Document,
                                              descriptors: MutableList<FoldingDescriptor>) {
        val type = node.elementType
        when(type) {
            LuaTypes.BLOCK -> {
                val block = node.psi as LuaBlock
                val prev = PsiTreeUtil.skipSiblingsBackward(block, PsiWhiteSpace::class.java)
                val next = PsiTreeUtil.skipSiblingsForward(block, PsiWhiteSpace::class.java)
                if (prev != null && next != null) {
                    val l = prev.textOffset + prev.textLength
                    val r = next.textOffset

                    val range = TextRange(l, r)
                    if (range.length > 0 && !addOneLineMethodFolding(descriptors, block, range, prev, next)) {
                        descriptors.add(FoldingDescriptor(block, range))
                    }
                }
            }
            LuaTypes.DOC_COMMENT -> {
                val textRange = node.textRange
                if (document.getLineNumber(textRange.startOffset) != document.getLineNumber(textRange.endOffset))
                    descriptors.add(FoldingDescriptor(node, textRange))
                return
            }
            LuaTypes.TABLE_EXPR -> {
                val textRange = node.textRange
                if (document.getLineNumber(textRange.startOffset) != document.getLineNumber(textRange.endOffset))
                    descriptors.add(FoldingDescriptor(node, textRange))
            }
        }

        for (child in node.getChildren(null)) {
            collectDescriptorsRecursively(child, document, descriptors)
        }
    }

    private fun addOneLineMethodFolding(descriptors: MutableList<FoldingDescriptor>, block: LuaBlock, range: TextRange, prev: PsiElement, next: PsiElement): Boolean {
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

            val lRange = TextRange(range.startOffset, first.textOffset)
            val rRange = TextRange(first.textOffset + first.textLength, range.endOffset)
            if (lRange.isEmpty || rRange.isEmpty)
                return false

            val group = FoldingGroup.newGroup("one-liner")
            descriptors.add(NamedFoldingDescriptor(prev.node, lRange, group, HOLDER_TEXT))
            descriptors.add(NamedFoldingDescriptor(next.node, rRange, group, HOLDER_TEXT))

            return true
        }

        return false
    }

    override fun isRegionCollapsedByDefault(astNode: ASTNode): Boolean {
        return astNode.elementType === LuaTypes.REGION
    }

    companion object {

        private val HOLDER_TEXT = "..."
    }
}
