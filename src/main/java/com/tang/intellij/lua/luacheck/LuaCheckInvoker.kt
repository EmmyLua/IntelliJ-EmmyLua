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

package com.tang.intellij.lua.luacheck

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.util.execution.ParametersListUtil
import org.intellij.lang.annotations.Language
import java.io.File

private val DEFAULT_ARGS = arrayOf("--codes", "--ranges")

private fun applyDefaultArgs(strArgs: String?): List<String> {
    val list:MutableList<String> = mutableListOf()
    if (strArgs != null) {
        val array = ParametersListUtil.parseToArray(strArgs)
        list.addAll(array)
    }
    list.addAll(DEFAULT_ARGS)
    return list.distinct()
}

fun runLuaCheck(project: Project, file: VirtualFile) {
    ToolWindowManager.getInstance(project).getToolWindow("LuaCheck").show {
        runLuaCheckInner(project, file)
    }
}

private fun runLuaCheckInner(project: Project, file: VirtualFile) {
    val settings = LuaCheckSettings.getInstance()
    val cmd = GeneralCommandLine(settings.luaCheck)
    val args = settings.luaCheckArgs
    cmd.addParameters(applyDefaultArgs(args))
    cmd.addParameter(file.name)
    cmd.workDirectory = File(file.parent.path)

    val checkView = ServiceManager.getService(project, LuaCheckView::class.java)
    val panel = checkView.panel
    val psiFile = PsiManagerEx.getInstance(project).findFile(file)!!
    val builder = panel.builder
    builder.clear()
    val fileNode = builder.addFile(psiFile)

    val handler = OSProcessHandler(cmd)
    @Language("RegExp")
    val reg = "(.+?):(\\d+):(\\d+)-(\\d+):(.+)\\n".toRegex()
    handler.addProcessListener(object : ProcessListener {
        override fun onTextAvailable(event: ProcessEvent, key: Key<*>?) {
            print(event.text)
            val matchResult = reg.find(event.text)
            if (matchResult != null) {
                //val matchGroup = matchResult.groups[1]!!
                val lineGroup = matchResult.groups[2]!!
                val colSGroup = matchResult.groups[3]!!
                val colEGroup = matchResult.groups[4]!!
                val descGroup = matchResult.groups[5]!!

                builder.addLCItem(LCRecordData(
                        lineGroup.value.toInt() - 1,
                        colSGroup.value.toInt() - 1,
                        colEGroup.value.toInt() - colSGroup.value.toInt() + 1,
                        descGroup.value), fileNode)
            }
        }

        override fun processTerminated(event: ProcessEvent) {
            ApplicationManager.getApplication().invokeLater { builder.performUpdate() }
        }

        override fun processWillTerminate(event: ProcessEvent, p1: Boolean) {

        }

        override fun startNotified(event: ProcessEvent) {

        }

    })
    handler.startNotify()
}