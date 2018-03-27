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

package com.tang.intellij.lua.debugger.app

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import com.tang.intellij.lua.debugger.DebuggerType
import com.tang.intellij.lua.debugger.IRemoteConfiguration
import com.tang.intellij.lua.debugger.LuaCommandLineState
import com.tang.intellij.lua.debugger.LuaRunConfiguration
import com.tang.intellij.lua.psi.LuaFileUtil
import org.jdom.Element
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 *
 * Created by tangzx on 2017/5/7.
 */
class LuaAppRunConfiguration(project: Project, factory: ConfigurationFactory)
    : LuaRunConfiguration(project, factory), IRemoteConfiguration {

    var program = PathEnvironmentVariableUtil.findInPath("lua")?.absolutePath
            ?: if (SystemInfoRt.isWindows) "lua.exe" else "lua"
    var file: String? = null
    var parameters: String? = null
    var charset: String = "UTF-8"
    var showConsole = true

    var debuggerType: DebuggerType = DebuggerType.Attach
        get() {
            if (!SystemInfoRt.isWindows && field == DebuggerType.Attach)
                field = DebuggerType.Mob
            return field
        }

    override fun getValidModules(): Collection<Module> {
        return emptyList()
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        val group = SettingsEditorGroup<LuaAppRunConfiguration>()
        group.addEditor("program", LuaAppSettingsEditor(project))
        return group
    }

    @Throws(ExecutionException::class)
    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? {
        return LuaCommandLineState(executionEnvironment)
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        JDOMExternalizerUtil.writeField(element, "program", program)
        JDOMExternalizerUtil.writeField(element, "file", file)
        JDOMExternalizerUtil.writeField(element, "workingDir", workingDir)
        JDOMExternalizerUtil.writeField(element, "debuggerType", debuggerType.value().toString())
        JDOMExternalizerUtil.writeField(element, "params", parameters)
        JDOMExternalizerUtil.writeField(element, "charset", charset)
        JDOMExternalizerUtil.writeField(element, "showConsole", if (showConsole) "true" else "false")
    }

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super.readExternal(element)
        JDOMExternalizerUtil.readField(element, "program")?.let { program = it }
        file = JDOMExternalizerUtil.readField(element, "file")
        workingDir = JDOMExternalizerUtil.readField(element, "workingDir")

        JDOMExternalizerUtil.readField(element, "debuggerType")
                ?.let { debuggerType = DebuggerType.valueOf(Integer.parseInt(it)) }

        parameters = JDOMExternalizerUtil.readField(element, "params")
        charset = JDOMExternalizerUtil.readField(element, "charset") ?: "UTF-8"
        showConsole = JDOMExternalizerUtil.readField(element, "showConsole") == "true"
    }

    override val port = 8172

    val virtualFile: VirtualFile?
        get() = LuaFileUtil.findFile(project, file)

    val parametersArray: Array<String>
        get() {
            val list = ArrayList<String>()
            if (false == parameters?.isEmpty()) {
                val strings = ParametersListUtil.parseToArray(parameters!!)
                list.addAll(Arrays.asList(*strings))
            }
            val file = file
            if (file != null && file.isNotEmpty()) {
                list.add(file)
            }
            return list.toTypedArray()
        }

    var workingDir: String? = null
        get() {
            val wd = field
            if (wd == null || wd.isEmpty()) {
                field = defaultWorkingDir
                return defaultWorkingDir
            }
            return wd
        }

    private val defaultWorkingDir: String?
        get() {
            val modules = ModuleManager.getInstance(project).modules
            for (module in modules) {
                val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots
                for (sourceRoot in sourceRoots) {
                    val path = sourceRoot.canonicalPath
                    if (path != null) {
                        return path
                    }
                }
            }
            return null
        }

    override fun checkConfiguration() {
        super.checkConfiguration()
        val program = program
        if (program.isEmpty()) {
            throw RuntimeConfigurationError("Program doesn't exist.")
        }

        val workingDir = workingDir
        if (workingDir == null || !File(workingDir).exists()) {
            throw RuntimeConfigurationError("Working dir doesn't exist.")
        }
    }

    override fun createCommandLine() = GeneralCommandLine().withExePath(program)
            .withEnvironment(envs)
            .withParameters(*parametersArray)
            .withWorkDirectory(workingDir)
            .withCharset(Charset.forName(charset))
}
