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

package com.tang.intellij.lua.codeInsight.ctrlFlow.instructions

import com.tang.intellij.lua.codeInsight.ctrlFlow.DataFlowRunner
import com.tang.intellij.lua.codeInsight.ctrlFlow.VMInstruction
import com.tang.intellij.lua.codeInsight.ctrlFlow.VMOffset
import com.tang.intellij.lua.codeInsight.ctrlFlow.VMState
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.RelationType
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.UnaryOp
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.VMValue
import com.tang.intellij.lua.psi.LuaBinaryExpr
import com.tang.intellij.lua.psi.LuaExpr
import com.tang.intellij.lua.psi.LuaPsiElement
import com.tang.intellij.lua.psi.LuaUnaryExpr

abstract class VMInstructionImpl : VMInstruction {

    override var index: Int = 0

    override fun accept(visitor: InstructionVisitor, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return emptyArray()
    }
}

abstract class BranchingInstruction(val anchor: LuaPsiElement) : VMInstructionImpl() {
    var trueReachable = false
    var falseReachable = false
    val allReachable get() = trueReachable && falseReachable
}

class GotoInstruction(val offset: VMOffset) : VMInstructionImpl() {
    override fun accept(visitor: InstructionVisitor, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return visitor.visitGoto(this, state, runner)
    }
}

// -0, +0
class ConditionGotoInstruction(val offset: VMOffset, condition: LuaExpr, val isNegated: Boolean = false) : BranchingInstruction(condition) {
    override fun accept(visitor: InstructionVisitor, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return visitor.visitConditionGoto(this, state, runner)
    }

    fun markReachable(isTrue: Boolean) {
        if ((isTrue && !isNegated) || (!isTrue && isNegated)) {
            trueReachable = true
        } else {
            falseReachable = true
        }
    }
}

// -0, +1
class PushInstruction(val value: VMValue) : VMInstructionImpl() {
    override fun accept(visitor: InstructionVisitor, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return visitor.visitPush(this, state, runner)
    }
}

class PopInstruction : VMInstructionImpl() {
    override fun accept(visitor: InstructionVisitor, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        state.pop()
        return visitor.nextInstruction(this, state, runner)
    }
}

//-1, +1
class UnaryInstruction(unaryExpr: LuaUnaryExpr) : VMInstructionImpl() {
    val op = UnaryOp.from(unaryExpr.unaryOp)

    override fun accept(visitor: InstructionVisitor, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return visitor.visitUnary(this, state, runner)
    }
}

//-2, +1
class BinaryInstruction(binaryExpr: LuaBinaryExpr) : VMInstructionImpl() {
    val op = RelationType.from(binaryExpr.binaryOp)

    override fun accept(visitor: InstructionVisitor, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return visitor.visitBinary(this, state, runner)
    }
}

//-2, +0
class AssignInstruction(val rExpr: LuaExpr?) : VMInstructionImpl() {
    override fun accept(visitor: InstructionVisitor, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return visitor.visitAssign(this, state, runner)
    }
}