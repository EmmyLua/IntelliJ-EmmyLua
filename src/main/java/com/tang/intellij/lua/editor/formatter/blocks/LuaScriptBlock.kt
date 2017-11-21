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
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.tang.intellij.lua.editor.formatter.LuaFormatContext
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.LuaTypes.*
import java.util.*

/**

 * Created by tangzx on 2016/12/3.
 */
open class LuaScriptBlock(val psi: PsiElement,
                          wrap: Wrap?,
                          private val alignment: Alignment?,
                          private val indent: Indent,
                          val ctx: LuaFormatContext) : AbstractBlock(psi.node, wrap, alignment) {

    companion object {
        //不创建 ASTBlock
        private val fakeBlockSet = TokenSet.create(BLOCK)

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
    }

    protected var childBlocks:List<LuaScriptBlock>? = null
    val elementType: IElementType = node.elementType

    private var next: LuaScriptBlock? = null
    private var prev: LuaScriptBlock? = null

    val nextBlock get() = next
    val prevBlock get() = prev

    protected fun getPrevSkipComment(): LuaScriptBlock? =
            if (prev?.psi is PsiComment) prev?.getPrevSkipComment() else prev

    protected fun getNextSkipComment(): LuaScriptBlock? =
            if (next?.psi is PsiComment) next?.getNextSkipComment() else next

    private var parent: LuaScriptBlock? = null
    val parentBlock get() = parent

    private fun shouldCreateBlockFor(node: ASTNode) =
            node.textRange.length != 0 && node.elementType !== TokenType.WHITE_SPACE

    override fun buildChildren(): List<Block> {
        if (childBlocks == null) {
            val blocks = ArrayList<LuaScriptBlock>()
            buildChildren(myNode.psi, blocks)
            childBlocks = blocks
            var prev: LuaScriptBlock? = null
            blocks.forEach {
                it.prev = prev
                prev?.next = it
                prev = it
            }
            postBuildChildren(blocks)
        }
        return childBlocks!!
    }

    protected open fun postBuildChildren(children: List<LuaScriptBlock>) {

    }

    private fun buildChildren(parent: PsiElement, results: MutableList<LuaScriptBlock>) {
        LuaPsiTreeUtil.processChildren(parent) { child ->
            val childType = child.node.elementType
            if (fakeBlockSet.contains(childType)) {
                LuaPsiTreeUtil.processChildren(child) {
                    if (shouldCreateBlockFor(it.node))
                        results.add(buildChild(it, Indent.getNormalIndent()))
                    true
                }
            } else if (shouldCreateBlockFor(child.node)) {
                results.add(buildChild(child))
            }
            true
        }
    }

    protected open fun buildChild(child:PsiElement, indent: Indent? = null): LuaScriptBlock {
        val childIndent = indent ?: Indent.getNoneIndent()
        return createBlock(child, childIndent, null)
    }

    protected fun createBlock(element: PsiElement, childIndent: Indent, alignment: Alignment? = null, wrap: Wrap? = null): LuaScriptBlock {
        val block = when (element) {
            is LuaUnaryExpr -> LuaUnaryExprBlock(element, wrap, alignment, childIndent, ctx)
            is LuaBinaryExpr -> LuaBinaryExprBlock(element, wrap, alignment, childIndent, ctx)
            is LuaParenExpr -> LuaParenExprBlock(element, wrap, alignment, childIndent, ctx)
            is LuaListArgs -> LuaListArgsBlock(element, wrap, alignment, childIndent, ctx)
            is LuaFuncBody -> LuaFuncBodyBlock(element, wrap, alignment, childIndent, ctx)
            is LuaTableExpr -> LuaTableBlock(element, wrap, alignment, childIndent, ctx)
            is LuaCallExpr -> LuaCallExprBlock(element, wrap, alignment, childIndent, ctx)
            is LuaIndentRange -> LuaIndentBlock(element, wrap, alignment, childIndent, ctx)
            is LuaIndexExpr -> LuaIndexExprBlock(element, wrap, alignment, childIndent, ctx)
            is LuaAssignStat,
            is LuaLocalDef -> LuaAssignBlock(element, wrap, alignment, childIndent, ctx)
            else -> LuaScriptBlock(element, wrap, alignment, childIndent, ctx)
        }
        block.parent = this
        return block
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        if ((child1 is LuaScriptBlock && child1.psi is LuaStatement) &&
                (child2 is LuaScriptBlock && child2.psi is LuaStatement)) {
            return Spacing.createSpacing(1, 0, 1, true, 1)
        }
        return ctx.spaceBuilder.getSpacing(this, child1, child2)
    }

    override fun getAlignment() = alignment

    override fun isLeaf() = myNode.firstChildNode == null

    override fun getIndent() = indent

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        if (childAttrSet.contains(elementType))
            return ChildAttributes(Indent.getNormalIndent(), null)
        return ChildAttributes(Indent.getNoneIndent(), null)
    }
}
