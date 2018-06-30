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

package com.tang.intellij.lua.codeInsight.ctrlFlow

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.codeInsight.ctrlFlow.instructions.*
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.*
import com.tang.intellij.lua.psi.*

class CtrlFlowProcessor(val factory: VMValueFactory) : LuaRecursiveVisitor() {

    private val instructions = mutableListOf<VMInstruction>()
    private val startOffsetMap = mutableMapOf<PsiElement, Int>()
    private val endOffsetMap = mutableMapOf<PsiElement, Int>()

    private fun <T : VMInstruction> addInstruction(instruction: T): T {
        instruction.index = instructions.size
        instructions.add(instruction)
        return instruction
    }

    fun getInstructions(): Array<VMInstruction> {
        return instructions.toTypedArray()
    }

    fun process(block: LuaBlock) {
        block.accept(this)
    }

    private fun getEndOffset(psi: PsiElement): VMOffset {
        return object : VMOffset {
            override val offset: Int
                get() = endOffsetMap[psi]!!
        }
    }

    private inline fun with(psi: PsiElement, action: () -> Unit) {
        startOffsetMap[psi] = instructions.size
        action()
        endOffsetMap[psi] = instructions.size
    }

    override fun visitLocalDef(o: LuaLocalDef) {
        with(o) {
            val list = o.exprList?.exprList
            var unsure = false
            o.nameList?.nameDefList?.forEachIndexed { index, def ->
                val varValue = factory.createVariableValue(def.name, def)
                // push variable
                addInstruction(PushInstruction(varValue))

                // push value
                val expr = list?.getOrNull(index)
                if (expr is LuaCallExpr) unsure = true
                when {
                    unsure -> pushUnknown()
                    expr == null -> pushNil()
                    else -> expr.accept(this)
                }

                // assign
                addInstruction(AssignInstruction(expr))
            }
        }
    }

    override fun visitAssignStat(o: LuaAssignStat) {
        with(o) {
            val list = o.varExprList.exprList
            val exprList = o.valueExprList?.exprList
            var unsure = false
            list.forEachIndexed { index, luaExpr ->
                luaExpr.accept(this)
                val expr = exprList?.getOrNull(index)
                if (expr is LuaCallExpr) unsure = true
                when {
                    unsure -> pushUnknown()
                    expr == null -> pushNil()
                    else -> expr.accept(this)
                }

                addInstruction(AssignInstruction(expr))
            }
        }
    }

    override fun visitNameExpr(o: LuaNameExpr) {
        with(o) {
            val variable = factory.createVariableValue(o.name, o)
            addInstruction(PushInstruction(variable))
        }
    }

    override fun visitTableExpr(o: LuaTableExpr) {
        with(o) {
            val map = FactMap().with(FactType.NULLABILITY, Nullability.NOT_NULL)
            val v = FactMapValue(map, factory)
            addInstruction(PushInstruction(v))
        }
    }

    override fun visitLiteralExpr(o: LuaLiteralExpr) {
        with(o) {
            addInstruction(PushInstruction(factory.createLiteralValue(o)))
        }
    }

    override fun visitBinaryExpr(o: LuaBinaryExpr) {
        with(o) {
            val l = o.left
            val r = o.right
            if (l == null || r == null) {
                pushUnknown()
            } else {
                val op = BinaryOp.from(o.binaryOp)
                when (op) {
                    BinaryOp.AND -> generateAndOr(l, r, o, true)
                    BinaryOp.OR -> generateAndOr(l, r, o, false)
                    else -> {
                        l.accept(this)
                        r.accept(this)
                        addInstruction(BinaryInstruction(o))
                    }
                }
            }
        }
    }

    private fun generateAndOr(l: LuaExpr, r: LuaExpr, o: LuaBinaryExpr, and: Boolean) {
        l.accept(this) // cond 1
        val endOffset = DeferredOffset(0)
        addInstruction(ConditionGotoInstruction(endOffset, l, and))
        addInstruction(PopInstruction())
        r.accept(this) // cond 2
        endOffset.offset = instructions.size
    }

    override fun visitUnaryExpr(o: LuaUnaryExpr) {
        with(o) {
            val expr = o.expr
            if (expr == null) {
                pushUnknown()
            } else {
                expr.accept(this)
                addInstruction(UnaryInstruction(o))
            }
        }
    }

    override fun visitCallExpr(o: LuaCallExpr) {
        with (o) {
            pushUnknown()
        }
    }

    override fun visitClassMethodDef(o: LuaClassMethodDef) {
        with(o) {

        }
    }

    override fun visitIfStat(o: LuaIfStat) {
        with(o) {
            val condition = PsiTreeUtil.findChildOfType(o, LuaExpr::class.java)
            if (condition != null) {
                // push condition
                condition.accept(this)
                val elseOffset = DeferredOffset(0)
                addInstruction(ConditionGotoInstruction(elseOffset, condition, true))
                // true body
                var cur: PsiElement? = condition
                while (cur != null) {
                    cur = cur.nextSibling
                    if (cur is LuaBlock)
                        cur.accept(this)
                }
                elseOffset.offset = instructions.size
            }
        }
    }

    override fun visitIndexExpr(o: LuaIndexExpr) {
        with(o) {
            o.firstChild.accept(this)
            addInstruction(CheckNotNilInstruction(o.firstChild))
            //o.idExpr?.accept(this)

            val offset = getEndOffset(o)
            addInstruction(GotoInstruction(offset))
        }
    }

    private fun pushUnknown() {
        addInstruction(PushInstruction(factory.constantValueFactory.UNKNOWN))
    }

    private fun pushNil() {
        addInstruction(PushInstruction(factory.constantValueFactory.NIL))
    }
}