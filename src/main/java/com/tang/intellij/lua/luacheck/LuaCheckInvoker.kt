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
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import java.io.File

fun runLuaCheck(file: VirtualFile) {
    val settings = LuaCheckSettings.getInstance()
    val cmd = GeneralCommandLine(settings.luaCheck)
    val args = settings.luaCheckArgs
    if (args != null) {
        cmd.addParameters(ParametersListUtil.parseToArray(args).toList())
    }
    cmd.addParameter(file.name)
    cmd.workDirectory = File(file.parent.path)

    val handler = OSProcessHandler(cmd)
    handler.addProcessListener(object : ProcessListener {
        override fun onTextAvailable(event: ProcessEvent, key: Key<*>?) {
            println(event.text)
        }

        override fun processTerminated(event: ProcessEvent) {

        }

        override fun processWillTerminate(event: ProcessEvent, p1: Boolean) {

        }

        override fun startNotified(event: ProcessEvent) {

        }

    })
    handler.startNotify()
}