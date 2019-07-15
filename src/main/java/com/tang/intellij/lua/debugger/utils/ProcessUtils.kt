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

package com.tang.intellij.lua.debugger.utils

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessInfo
import com.intellij.execution.util.ExecUtil
import com.tang.intellij.lua.psi.LuaFileUtil

data class ProcessDetailInfo(
        var pid: Int = 0,
        var path: String = "",
        var title: String = ""
)

private const val MAX_DISPLAY_LEN = 60

fun getDisplayName(processInfo: ProcessInfo, detailInfo: ProcessDetailInfo): String {
    val s = if (detailInfo.title.isNotEmpty())
        "${processInfo.executableName} - ${detailInfo.title}"
    else processInfo.executableName

    if (s.length > MAX_DISPLAY_LEN)
        return "${s.substring(0, MAX_DISPLAY_LEN)}..."
    return s
}

fun listProcesses(): Map<Int, ProcessDetailInfo> {
    val processMap = mutableMapOf<Int, ProcessDetailInfo>()
    val archExe = LuaFileUtil.archExeFile ?: return processMap
    val commandLine = GeneralCommandLine(archExe)
    //commandLine.charset = Charset.forName("UTF-8")
    commandLine.addParameters("list_processes")

    val processOutput = ExecUtil.execAndGetOutput(commandLine)

    val text = processOutput.stdout
    val lines = text.split("\n")
    val size = lines.size / 4
    for (i in 0 until size) {
        val pid = lines[i * 4 + 0].toInt()
        val title = lines[i * 4 + 1]
        val path = lines[i * 4 + 2]
        val p = ProcessDetailInfo(pid, path, title)
        processMap[pid] = p
    }
    return processMap
}