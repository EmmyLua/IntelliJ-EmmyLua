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
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.VMValueFactory
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.VMValueFactoryImpl
import com.tang.intellij.lua.psi.LuaBlock
import java.util.concurrent.LinkedBlockingDeque

class DataFlowRunner {
    val factory: VMValueFactory = VMValueFactoryImpl()
    private val myCtrlFlow = CtrlFlowProcessor(factory)
    private var myInstructions = emptyArray<VMInstruction>()

    val instructions: Array<VMInstruction> get() = myCtrlFlow.getInstructions()

    fun analyzeBlock(psi: LuaBlock, visitor: InstructionVisitor) {
        myCtrlFlow.process(psi)
        myInstructions = myCtrlFlow.getInstructions()
        if (myInstructions.isNotEmpty()) {
            val state = VMStateImpl(factory)

            val queue = LinkedBlockingDeque<InstructionState>()
            queue.offer(InstructionState(0, state))
            while (queue.isNotEmpty()) {
                val instructionState = queue.remove()
                val instruction = getInstruction(instructionState.instructionIndex) ?: continue
                val incomingStates = instruction.accept(visitor, instructionState.state, this)
                incomingStates.forEach { queue.offer(it) }
            }
        }
    }

    private fun getInstruction(index: Int): VMInstruction? {
        return myInstructions.getOrNull(index)
    }
}