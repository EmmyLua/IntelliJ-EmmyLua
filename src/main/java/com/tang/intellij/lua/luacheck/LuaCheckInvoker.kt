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
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.util.execution.ParametersListUtil
import com.tang.intellij.lua.lang.LuaFileType
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
        var dir: VirtualFile = file.parent
        val list: MutableList<Pair<String, PsiFile>> = mutableListOf()
        if (file.isDirectory) {
            val dirPath = file.canonicalPath!!
            dir = file
            VfsUtil.visitChildrenRecursively(dir, object: VirtualFileVisitor<VirtualFile>() {
                override fun visitFile(vf: VirtualFile): Boolean {
                    if (vf.fileType == LuaFileType.INSTANCE) {
                        val psiFile = PsiManagerEx.getInstance(project).findFile(vf)
                        if (psiFile != null) {
                            val path = vf.canonicalPath!!
                            list.add(Pair(path.substring(dirPath.length + 1), psiFile))
                        }
                    }
                    return true
                }
            })
        } else {
            val psiFile = PsiManagerEx.getInstance(project).findFile(file)
            if (psiFile != null)
                list.add(Pair(psiFile.name, psiFile))
        }

        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            val indicator = ProgressManager.getInstance().progressIndicator
            runLuaCheck(project, list.toTypedArray(), dir, indicator)
        }, "LuaCheck", true, project)
    }
}

private fun runLuaCheck(project: Project,
                        fileList: Array<Pair<String, PsiFile>>,
                        dir: VirtualFile,
                        indicator: ProgressIndicator) {
    val checkView = ServiceManager.getService(project, LuaCheckView::class.java)
    val panel = checkView.panel
    val builder = panel.builder
    builder.clear()

    var idx = 0
    for ((first, second) in fileList) {
        if (indicator.isCanceled) {
            break
        }
        indicator.text = second.name
        indicator.fraction = idx.toDouble() / fileList.size
        idx++
        runLuaCheckInner(first, second, dir, builder)
    }

    ApplicationManager.getApplication().invokeLater { builder.performUpdate() }
}

private fun runLuaCheckInner(relativeFilePath: String,
                             file: PsiFile,
                             dir: VirtualFile,
                             builder: LuaCheckTreeBuilder) {
    val settings = LuaCheckSettings.getInstance()
    val cmd = GeneralCommandLine(settings.luaCheck)
    val args = settings.luaCheckArgs
    cmd.addParameters(applyDefaultArgs(args))
    cmd.addParameter(relativeFilePath)
    cmd.workDirectory = File(dir.path)

    val fileNode = builder.addFile(file)

    val handler = OSProcessHandler(cmd)
    @Language("RegExp")
    val reg = "(.+?):(\\d+):(\\d+)-(\\d+):(.+)\\n".toRegex()
    handler.addProcessListener(object : ProcessListener {
        override fun onTextAvailable(event: ProcessEvent, key: Key<*>) {
            //print(event.text)
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

        }

        override fun processWillTerminate(event: ProcessEvent, p1: Boolean) {

        }

        override fun startNotified(event: ProcessEvent) {

        }
    })
    handler.startNotify()
    handler.waitFor()
}