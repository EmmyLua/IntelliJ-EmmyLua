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

package com.tang.intellij.lua.editor.formatter

import com.intellij.configurationStore.NOTIFICATION_GROUP_ID
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.psi.PsiFile
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaFileUtil.getPluginVirtualFile
import com.tang.intellij.lua.psi.LuaPsiFile
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*


class EmmyLuaCodeStyle : AsyncDocumentFormattingService() {

    private val FEATURES: MutableSet<FormattingService.Feature> = EnumSet.noneOf(
        FormattingService.Feature::class.java
    )

    override fun getFeatures(): MutableSet<FormattingService.Feature> {
        return FEATURES
    }

    override fun canFormat(file: PsiFile): Boolean {
        return file is LuaPsiFile
    }

    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val targetFile = request.ioFile ?: return null
        val path = targetFile.path
        var exePath: String? = null
        if (SystemInfoRt.isWindows)
            exePath = getPluginVirtualFile("formatter/emmy/win32-x64/bin/CodeFormat.exe")
        else if (SystemInfoRt.isLinux) {
            exePath = getPluginVirtualFile("formatter/emmy/linux-x64/bin/CodeFormat")
        } else if (SystemInfoRt.isMac) {
            if (System.getProperty("os.arch") == "arm64") {
                exePath = getPluginVirtualFile("formatter/emmy/darwin-arm64/bin/CodeFormat")
            } else {
                exePath = getPluginVirtualFile("formatter/emmy/darwin-x64/bin/CodeFormat")
            }
        }

        if (exePath == null) {
            return null;
        }

        if (!File(exePath).canExecute()) {
            File(exePath).setExecutable(true)
        }

        val project = request.context.project
        val params = mutableListOf<String>(
            "format",
            "-f",
            path,
        )
        if (project.basePath != null) {
            // detect-config
            params.add("-d")
            val settings = request.context.codeStyleSettings
            val luaCodeStyleSettings = settings.getCustomSettings(LuaCodeStyleSettings::class.java)
            val commonSettings = settings.getCommonSettings(LuaLanguage.INSTANCE)
            commonSettings.indentOptions?.let {
                params.add(
                    if (it.USE_TAB_CHARACTER) "--indent_style=tab"
                    else "--indent_style=space"
                )

                var tab_size = it.TAB_SIZE
                var indent_size = it.INDENT_SIZE

                if(tab_size < 0 || tab_size > 8){
                    tab_size = 8
                }

                if(indent_size < 0 || indent_size > 8){
                    indent_size = 4
                }

                params.add(
                    if (it.USE_TAB_CHARACTER) "--tab_width=$tab_size"
                    else "--indent_size=$indent_size"
                )

                var continuation = it.CONTINUATION_INDENT_SIZE
                if (continuation > 8 || continuation < 0) {
                    continuation = 8
                }

                params.add(
                    "--continuation_indent_size=$continuation"
                )
                true
            }
            CodeStyleSettingOptions.Instance.makeCommandLineParams(luaCodeStyleSettings, params)
        }

        try {
            val commandLine = GeneralCommandLine()
                .withExePath(exePath)
                .withParameters(
                    params
                )
            val handler = OSProcessHandler(commandLine.withCharset(StandardCharsets.UTF_8))

            return object : FormattingTask {
                override fun run() {
                    handler.addProcessListener(object : CapturingProcessAdapter() {
                        override fun processTerminated(event: ProcessEvent) {
                            val exitCode: Int = event.exitCode
                            if (exitCode == 0) {
                                request.onTextReady(output.stdout)
                            } else {
                                request.onError("EmmyLuaCodeStyle", output.stderr)
                            }
                        }
                    })
                    handler.startNotify()
                }

                override fun cancel(): Boolean {
                    handler.destroyProcess()
                    return true
                }

                override fun isRunUnderProgress(): Boolean {
                    return true
                }
            }

        } catch (e: ExecutionException) {
            e.message?.let { request.onError("EmmyLuaCodeStyle", it) };
            return null;
        }

    }

    override fun getNotificationGroupId(): String {
        return NOTIFICATION_GROUP_ID
    }

    override fun getName(): String {
        return "EmmyLuaCodeStyle"
    }
}