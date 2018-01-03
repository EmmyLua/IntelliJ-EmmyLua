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
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.psi.LuaTypes

class LuaRegionFoldingBuilder : FoldingBuilderEx() {

    private val customFoldingBuilder = object : CustomFoldingBuilder() {
        override fun isRegionCollapsedByDefault(astNode: ASTNode): Boolean {
            return astNode.elementType === LuaTypes.REGION
        }

        override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {

        }

        override fun getLanguagePlaceholderText(p0: ASTNode, p1: TextRange): String {
            return "..."
        }
    }

    override fun getPlaceholderText(astNode: ASTNode) = customFoldingBuilder.getPlaceholderText(astNode)

    override fun getPlaceholderText(node: ASTNode, range: TextRange) = customFoldingBuilder.getPlaceholderText(node, range)

    override fun isCollapsedByDefault(astNode: ASTNode): Boolean {
        return customFoldingBuilder.isCollapsedByDefault(astNode)
    }

    override fun buildFoldRegions(element: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        return if (quick)
            emptyArray()
        else
            customFoldingBuilder.buildFoldRegions(element, document, quick)
    }
}