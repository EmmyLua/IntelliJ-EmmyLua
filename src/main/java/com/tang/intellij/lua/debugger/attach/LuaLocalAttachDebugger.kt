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

package com.tang.intellij.lua.debugger.attach

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessInfo
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.attach.XLocalAttachDebugger
import com.tang.intellij.lua.debugger.utils.ProcessDetailInfo
import com.tang.intellij.lua.debugger.utils.getDisplayName

/**
 *
 * Created by tangzx on 2017/2/28.
 */
class LuaLocalAttachDebugger internal constructor(private val processInfo: ProcessInfo, private val detailInfo: ProcessDetailInfo) : XLocalAttachDebugger {

    override fun getDebuggerDisplayName() =
            getDisplayName(processInfo, detailInfo)

    @Throws(ExecutionException::class)
    override fun attachDebugSession(project: Project, processInfo: ProcessInfo) {
        val displayName = "PID:${processInfo.pid}($debuggerDisplayName)"
        XDebuggerManager.getInstance(project).startSessionAndShowTab(displayName, null, object : XDebugProcessStarter() {
            @Throws(ExecutionException::class)
            override fun start(xDebugSession: XDebugSession): XDebugProcess =
                    LuaAttachDebugProcess(xDebugSession, processInfo)
        })
    }
}
