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

package com.tang.intellij.lua.debugger.emmy

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.tang.intellij.lua.debugger.LuaCommandLineState
import com.tang.intellij.lua.debugger.LuaRunConfiguration
import com.tang.intellij.lua.lang.LuaIcons
import org.jdom.Element
import javax.swing.Icon

class EmmyDebugConfigurationType : ConfigurationType {
    override fun getIcon(): Icon {
        return LuaIcons.FILE
    }

    override fun getConfigurationTypeDescription(): String {
        return "Emmy Debugger(NEW)"
    }

    override fun getId(): String {
        return "lua.emmy.debugger"
    }

    override fun getDisplayName(): String {
        return "Emmy Debugger(NEW)"
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(EmmyDebuggerConfigurationFactory(this))
    }
}

enum class EmmyDebugTransportType(val desc: String) {
    TCP_CLIENT("Tcp ( IDE connect debugger )"),
    TCP_SERVER("Tcp ( Debugger connect IDE )"),
    PIPE_CLIENT("Pipeline ( IDE connect debugger )"),
    PIPE_SERVER("Pipeline ( Debugger connect IDE )");

    override fun toString(): String {
        return desc
    }
}

enum class EmmyWinArch(val desc: String) {
    X86("x86"),
    X64("x64");

    override fun toString(): String {
        return desc;
    }
}

class EmmyDebuggerConfigurationFactory(val type: EmmyDebugConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return EmmyDebugConfiguration(project, this)
    }
}

class EmmyDebugConfiguration(project: Project, factory: EmmyDebuggerConfigurationFactory) : LuaRunConfiguration(project, factory) {
    var type = EmmyDebugTransportType.TCP_SERVER

    var host = "localhost"
    var port = 9966
    var winArch = EmmyWinArch.X64
    var pipeName = "emmy"

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        val group = SettingsEditorGroup<EmmyDebugConfiguration>()
        group.addEditor("emmy", EmmyDebugSettingsPanel(project))
        return group
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return LuaCommandLineState(environment)
    }

    override fun getValidModules(): Collection<Module> {
        return emptyList()
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        JDOMExternalizerUtil.writeField(element, "TYPE", type.ordinal.toString())
        JDOMExternalizerUtil.writeField(element, "HOST", host)
        JDOMExternalizerUtil.writeField(element, "PORT", port.toString())
        JDOMExternalizerUtil.writeField(element, "PIPE", pipeName)
        JDOMExternalizerUtil.writeField(element, "WIN_ARCH", winArch.ordinal.toString())
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        JDOMExternalizerUtil.readField(element, "HOST")?.let {
            host = it
        }
        JDOMExternalizerUtil.readField(element, "PORT")?.let {
            port = it.toInt()
        }
        JDOMExternalizerUtil.readField(element, "PIPE")?.let {
            pipeName = it
        }
        JDOMExternalizerUtil.readField(element, "TYPE")?.let { value ->
            val i = value.toInt()
            type = EmmyDebugTransportType.values().find { it.ordinal == i } ?: EmmyDebugTransportType.TCP_SERVER
        }
        JDOMExternalizerUtil.readField(element, "WIN_ARCH")?.let { value ->
            val i = value.toInt()
            winArch = EmmyWinArch.values().find { it.ordinal == i } ?: EmmyWinArch.X64
        }
    }
}