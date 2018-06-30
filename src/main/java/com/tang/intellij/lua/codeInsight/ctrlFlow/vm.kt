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

import com.tang.intellij.lua.codeInsight.ctrlFlow.instructions.InstructionState
import com.tang.intellij.lua.codeInsight.ctrlFlow.instructions.InstructionVisitor
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.*

interface VMInstruction {
    var index: Int

    fun accept(visitor: InstructionVisitor, state: VMState, runner: DataFlowRunner):  Array<InstructionState>
}

interface VMState {
    val id: Int

    fun push(value: VMValue)

    fun pop(): VMValue

    fun peek(): VMValue

    fun createCopy(): VMState

    fun createClosure(): VMState

    fun castCondition(value: VMValue): ConditionValue

    fun setVariableValue(variable: VariableValue, value: VMValue)

    fun flushVariable(variable: VariableValue)

    fun getValueState(variable: VariableValue): ValueState?

    fun <T> setVariableFact(variable: VariableValue, factType: FactType<T>, value: T?)

    fun applyCondition(cond: ConditionValue): Boolean

    fun isNil(value: VMValue): Boolean

    fun isNotNil(value: VMValue): Boolean
}

interface VMOffset {
    val offset: Int
}

class DeferredOffset(override var offset: Int) : VMOffset