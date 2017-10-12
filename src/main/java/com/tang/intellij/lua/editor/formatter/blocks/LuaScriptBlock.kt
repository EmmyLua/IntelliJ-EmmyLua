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

package com.tang.intellij.lua.editor.formatter.blocks

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.tang.intellij.lua.editor.formatter.LuaFormatContext
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.psi.LuaTypes.*
import java.util.*

/**

 * Created by tangzx on 2016/12/3.
 */
open class LuaScriptBlock(private val parent: LuaScriptBlock?,
                          node: ASTNode,
                          wrap: Wrap?,
                          private val alignment: Alignment?,
                          private val indent: Indent,
                          private val ctx: LuaFormatContext) : AbstractBlock(node, wrap, alignment) {

    //不创建 ASTBlock
    private val fakeBlockSet = TokenSet.create(
            BLOCK
    )

    //回车时
    private val childAttrSet = TokenSet.orSet(fakeBlockSet, TokenSet.create(
            IF_STAT,
            DO_STAT,
            FUNC_BODY,
            FOR_A_STAT,
            FOR_B_STAT,
            REPEAT_STAT,
            WHILE_STAT,
            TABLE_EXPR,
            ARGS
    ))

    private val elementType: IElementType = node.elementType

    private fun shouldCreateBlockFor(node: ASTNode): Boolean {
        return node.textRange.length != 0 && node.elementType !== TokenType.WHITE_SPACE
    }

    override fun buildChildren(): List<Block> {
        val blocks = ArrayList<Block>()
        buildChildren(myNode, blocks)
        return blocks
    }

    private fun buildChildren(parent: ASTNode, results: MutableList<Block>) {
        var child: ASTNode? = parent.firstChildNode
        val parentType = parent.elementType
        var childIndent = Indent.getNoneIndent()
        if (fakeBlockSet.contains(parentType)) {
            childIndent = Indent.getNormalIndent()
        }

        while (child != null) {
            val childType = child.elementType
            if (parentType == TABLE_EXPR ) {
                if (childType != LCURLY && childType != RCURLY)
                    childIndent = Indent.getNormalIndent()
                else
                    childIndent = Indent.getNoneIndent()
            }

            if (fakeBlockSet.contains(childType)) {
                buildChildren(child, results)
            } else if (shouldCreateBlockFor(child)) {
                results.add(createBlock(child, childIndent, null))
            }
            child = child.treeNext
        }
    }

    private fun createBlock(node: ASTNode, childIndent: Indent, alignment: Alignment?): LuaScriptBlock {
        return when (node.elementType) {
            UNARY_EXPR -> LuaUnaryScriptBlock(this, node, null, alignment, childIndent, ctx)
            BINARY_EXPR -> LuaBinaryScriptBlock(this, node, null, alignment, childIndent, ctx)
            else -> LuaScriptBlock(this, node, null, alignment, childIndent, ctx)
        }
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        if (this.myNode.elementType === CALL_EXPR) {
            if (child1 is LuaScriptBlock && child2 is LuaScriptBlock) {
                // call(param)
                if (child2.myNode.findChildByType(LuaTypes.LPAREN) != null) {
                    return Spacing.createSpacing(0, 0, 0, false, 0)
                } else {
                    return Spacing.createSpacing(1, 1, 0, false, 0)
                }// call "string"
            }
        }

        return ctx.spaceBuilder.getSpacing(this, child1, child2)
    }

    override fun getAlignment(): Alignment? {
        return alignment
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }

    override fun getIndent(): Indent? {
        return indent
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        if (childAttrSet.contains(elementType))
            return ChildAttributes(Indent.getNormalIndent(), null)
        return ChildAttributes(Indent.getNoneIndent(), null)
    }
}
