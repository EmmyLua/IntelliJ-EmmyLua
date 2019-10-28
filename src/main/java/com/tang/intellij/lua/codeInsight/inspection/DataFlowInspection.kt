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

package com.tang.intellij.lua.codeInsight.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.codeInsight.ctrlFlow.DataFlowRunner
import com.tang.intellij.lua.codeInsight.ctrlFlow.instructions.BranchingInstruction
import com.tang.intellij.lua.codeInsight.ctrlFlow.instructions.CheckNotNilInstruction
import com.tang.intellij.lua.codeInsight.ctrlFlow.instructions.InstructionVisitor
import com.tang.intellij.lua.psi.LuaBlock
import com.tang.intellij.lua.psi.LuaDoStat
import com.tang.intellij.lua.psi.LuaPsiElement
import com.tang.intellij.lua.psi.LuaVisitor

class DataFlowInspection : LocalInspectionTool() {
    private var holder: ProblemsHolder? = null

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        this.holder = holder
        return object : LuaVisitor() {
            override fun visitPsiElement(o: LuaPsiElement) {
                if (o is LuaDoStat) {
                    val runner = DataFlowRunner()
                    val visitor = DataFlowInstructionVisitor()
                    val block = PsiTreeUtil.getChildOfType(o, LuaBlock::class.java)
                    if (block != null) analyzeBlock(block, runner, visitor)
                } else
                    super.visitPsiElement(o)
            }
        }
    }

    private fun analyzeBlock(block: LuaBlock, runner: DataFlowRunner, visitor: DataFlowInstructionVisitor) {
        runner.analyzeBlock(block, visitor)

        val instructions = runner.instructions
        instructions
                .filterIsInstance(BranchingInstruction::class.java)
                .filter { !it.allReachable }
                .forEach {
                    if (it.trueReachable) {
                        holder?.registerProblem(it.anchor, "always true")
                    } else {
                        holder?.registerProblem(it.anchor, "always false")
                    }
                }
        instructions
                .filterIsInstance(CheckNotNilInstruction::class.java)
                .filter { it.isNil }
                .forEach {
                    holder?.registerProblem(it.anchor, "always nil")
                }
    }
}

class DataFlowInstructionVisitor : InstructionVisitor() {

}