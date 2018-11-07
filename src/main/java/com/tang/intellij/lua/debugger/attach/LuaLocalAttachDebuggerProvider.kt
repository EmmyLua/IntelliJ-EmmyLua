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

import com.intellij.execution.process.ProcessInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.xdebugger.attach.XLocalAttachDebugger
import com.intellij.xdebugger.attach.XLocalAttachDebuggerProvider
import com.intellij.xdebugger.attach.XLocalAttachGroup
import com.tang.intellij.lua.debugger.utils.ProcessDetailInfo
import com.tang.intellij.lua.debugger.utils.ProcessUtils
import java.util.*

/**
 *
 * Created by tangzx on 2017/2/28.
 */
class LuaLocalAttachDebuggerProvider : XLocalAttachDebuggerProvider {

    override fun getAttachGroup(): XLocalAttachGroup = LuaLocalAttachGroup.INSTANCE

    companion object {
        val DETAIL_KEY = Key.create<Map<Int, ProcessDetailInfo>>("LuaLocalAttachDebuggerProvider.key")
    }

    private var processMap = mapOf<Int, ProcessDetailInfo>()

    override fun getAvailableDebuggers(project: Project, processInfo: ProcessInfo, userDataHolder: UserDataHolder): List<XLocalAttachDebugger> {

        val b = userDataHolder.getUserData(DETAIL_KEY)
        if (b == null) {
            processMap = ProcessUtils.listProcesses()
            userDataHolder.putUserData(DETAIL_KEY, processMap)
        }

        if (processInfo.executableName.endsWith(".exe")) {
            val list = ArrayList<XLocalAttachDebugger>()
            val info = processMap[processInfo.pid]
            if (info != null
                    && info.path.isNotEmpty()
                    && !info.path.contains("windows", true)) {
                list.add(LuaLocalAttachDebugger(processInfo, info))
            }
            return list
        }

        return emptyList()
    }
}
