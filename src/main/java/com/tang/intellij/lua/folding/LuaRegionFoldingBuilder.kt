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
import com.intellij.lang.customFolding.VisualStudioCustomFoldingProvider
import com.intellij.lang.folding.CompositeFoldingBuilder
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.psi.LuaTypes
import java.util.*

class LuaRegionFoldingBuilder : FoldingBuilderEx() {

    data class FoldingStack(val owner: ASTNode) : Stack<ASTNode>()

    private val customFoldingBuilder = object : FoldingBuilderEx() {
        private val providerA = LuaFoldingProvider()
        private val providerB = VisualStudioCustomFoldingProvider()
        private val myMaxLookupDepth = Registry.get("custom.folding.max.lookup.depth")
        private val ourCustomRegionElements = ThreadLocal<MutableSet<ASTNode>>()

        override fun getPlaceholderText(node: ASTNode): String {
            val text = node.text
            return if (providerA.isCustomRegionStart(text))
                providerA.getPlaceholderText(text)
            else providerB.getPlaceholderText(text)
        }

        override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
            ourCustomRegionElements.set(mutableSetOf())
            val descriptors = mutableListOf<FoldingDescriptor>()

            try {
                val rootNode = root.node
                if (rootNode != null) {
                    this.addCustomFoldingRegionsRecursively(FoldingStack(rootNode), rootNode, descriptors, 0)
                }
            } finally {
                ourCustomRegionElements.set(null)
            }

            return descriptors.toTypedArray()
        }

        private fun addCustomFoldingRegionsRecursively(foldingStack: FoldingStack, node: ASTNode, descriptors: MutableList<in FoldingDescriptor>, currDepth: Int) {
            val localFoldingStack = if (this.isCustomFoldingRoot(node)) FoldingStack(node) else foldingStack

            var child: ASTNode? = node.firstChildNode
            while (child != null) {
                if (this.isCustomRegionStart(child)) {
                    localFoldingStack.push(child)
                } else if (this.isCustomRegionEnd(child)) {
                    if (!localFoldingStack.isEmpty()) {
                        val startNode = localFoldingStack.pop() as ASTNode
                        val startOffset = startNode.textRange.startOffset
                        val range = TextRange(startOffset, child.textRange.endOffset)
                        startNode.psi.putUserData(CompositeFoldingBuilder.FOLDING_BUILDER, this)
                        descriptors.add(FoldingDescriptor(startNode, range))
                        val nodeSet = ourCustomRegionElements.get()
                        nodeSet.add(startNode)
                        nodeSet.add(child)
                    }
                } else if (currDepth < myMaxLookupDepth.asInteger()) {
                    this.addCustomFoldingRegionsRecursively(localFoldingStack, child, descriptors, currDepth + 1)
                }
                child = child.treeNext
            }

        }

        private fun isCustomFoldingRoot(node: ASTNode): Boolean {
            return node.firstChildNode != null
        }

        fun isCustomRegionStart(node: ASTNode): Boolean {
            return if (!this.isCustomFoldingCandidate(node)) {
                false
            } else {
                val nodeText = node.text
                providerA.isCustomRegionStart(nodeText) || providerB.isCustomRegionStart(nodeText)
            }
        }

        private fun isCustomRegionEnd(node: ASTNode): Boolean {
            return if (!this.isCustomFoldingCandidate(node)) {
                false
            } else {
                val nodeText = node.text
                providerA.isCustomRegionEnd(nodeText) || providerB.isCustomRegionEnd(nodeText)
            }
        }

        override fun isCollapsedByDefault(node: ASTNode) = false

        fun isCustomFoldingCandidate(node: ASTNode): Boolean {
            return node.elementType == LuaTypes.REGION || node.elementType == LuaTypes.ENDREGION
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