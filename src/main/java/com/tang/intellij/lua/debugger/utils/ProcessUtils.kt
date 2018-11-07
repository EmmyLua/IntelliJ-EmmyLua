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
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory


data class ProcessDetailInfo(
        var pid: Int = 0,
        var path: String = "",
        var title: String = ""
)

private const val MAX_DISPLAY_LEN = 60

internal fun getDisplayName(processInfo: ProcessInfo, detailInfo: ProcessDetailInfo): String {
    val s = if (detailInfo.title.isNotEmpty())
        "${processInfo.executableName} - ${detailInfo.title}"
    else processInfo.executableName

    if (s.length > MAX_DISPLAY_LEN)
        return "${s.substring(0, MAX_DISPLAY_LEN)}..."
    return s
}


class ProcessUtils {
    companion object {
        fun listProcesses(): Map<Int, ProcessDetailInfo> {
            var processMap = mutableMapOf<Int, ProcessDetailInfo>()
            val archExe = LuaFileUtil.getArchExeFile()
            val commandLine = GeneralCommandLine(archExe)
            //commandLine.charset = Charset.forName("UTF-8")
            commandLine.addParameters("list_processes")

            val processOutput = ExecUtil.execAndGetOutput(commandLine)

            val text = processOutput.stdout
            val builder = DocumentBuilderFactory.newInstance()
            val documentBuilder = builder.newDocumentBuilder()
            val document = documentBuilder.parse(ByteArrayInputStream(text.toByteArray()))
            val root = document.documentElement
            root.childNodes.let {
                for (i in 0 until it.length) {
                    val c = it.item(i)
                    val p = ProcessDetailInfo()
                    val map = c.attributes
                    map.getNamedItem("pid")?.let {
                        p.pid = it.nodeValue.toInt()
                    }
                    val childNodes = c.childNodes
                    for (j in 0 until childNodes.length) {
                        val child = childNodes.item(j)
                        when (child.nodeName) {
                            "title" -> {
                                val item = child.childNodes.item(0)
                                p.title = item?.nodeValue ?: ""
                            }
                            "path" -> {
                                val item = child.childNodes.item(0)
                                p.path = item?.nodeValue ?: ""
                            }
                        }
                    }
                    processMap[p.pid] = p
                }
            }
            return processMap
        }
    }
}