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
import com.tang.intellij.lua.codeInsight.ctrlFlow.VMState
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.UnaryOp
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.VariableValue

open class InstructionVisitor {
    fun nextInstruction(instruction: VMInstruction, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return arrayOf(InstructionState(instruction.index + 1, state))
    }

    private fun nextInstruction(index: Int, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return arrayOf(InstructionState(index, state))
    }

    fun visitPush(instruction: PushInstruction, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        state.push(instruction.value)
        return nextInstruction(instruction, state, runner)
    }

    fun visitAssign(instruction: AssignInstruction, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        // variable
        val r = state.pop()
        // value
        val l = state.pop()
        if (l is VariableValue) {
            state.setVariableValue(l, r)
        }
        return nextInstruction(instruction, state, runner)
    }

    fun visitGoto(goto: GotoInstruction, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return nextInstruction(goto.offset.offset, state, runner)
    }

    fun visitConditionGoto(instruction: ConditionGotoInstruction, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        // cond
        val cond = state.castCondition(state.peek())
        val condTrue = if (instruction.isNegated) cond.createNegated() else cond
        val condFalse = if (instruction.isNegated) cond else cond.createNegated()

        val elseState = state.createCopy()
        val branches = mutableListOf<InstructionState>()
        if (state.applyCondition(condTrue)) {
            instruction.markReachable(true)
            branches += nextInstruction(instruction.offset.offset, state, runner)
        }

        if (elseState.applyCondition(condFalse)) {
            instruction.markReachable(false)
            branches += nextInstruction(instruction, elseState, runner)
        }
        return branches.toTypedArray()
    }

    fun visitBoolConversion(instruction: BoolConversionInstruction, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        return nextInstruction(instruction, state, runner)
    }

    fun visitBinary(instruction: BinaryInstruction, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        val r = state.pop()
        val l = state.pop()
        val op = instruction.op
        state.push(runner.factory.binaryOpValueFactory.create(l, r, op))
        return nextInstruction(instruction, state, runner)
    }

    fun visitUnary(instruction: UnaryInstruction, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        val value = state.pop()
        val op = instruction.op
        if (op == UnaryOp.NOT) {
            // not a
            state.push(state.castCondition(value).createNegated())
        } else {
            state.push(runner.factory.constantValueFactory.UNKNOWN)
        }
        return nextInstruction(instruction, state, runner)
    }

    fun visitCheckNotNil(instruction: CheckNotNilInstruction, state: VMState, runner: DataFlowRunner): Array<InstructionState> {
        if (state.isNil(state.peek())) {
            instruction.isNil = true
        }
        return nextInstruction(instruction, state, runner)
    }
}