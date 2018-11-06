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

package com.tang.intellij.lua.debugger.remote

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.openapi.util.WriteExternalException
import com.tang.intellij.lua.debugger.LuaCommandLineState
import com.tang.intellij.lua.debugger.LuaRunConfiguration
import com.tang.intellij.lua.debugger.unity.LuaUnitySettingsEditor
import org.jdom.Element

/**
 *
 * Created by Taigacon on 2018/11/6.
 */
class LuaUnityConfiguration(project: Project, factory: ConfigurationFactory)
    : LuaRunConfiguration(project, factory), RunConfigurationWithSuppressedDefaultRunAction {

    var preferedUnityInstanceName: String = ""

    override fun checkConfiguration() {
        super.checkConfiguration()
        checkSourceRoot()
    }

    override fun getValidModules(): Collection<Module> {
        //final Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        return emptyList()
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        val group = SettingsEditorGroup<LuaUnityConfiguration>()
        group.addEditor("unity", LuaUnitySettingsEditor())
        return group
    }

    @Throws(ExecutionException::class)
    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? {
        return LuaCommandLineState(executionEnvironment)
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        JDOMExternalizerUtil.writeField(element, "PreferedUnityInstanceName", preferedUnityInstanceName)
    }

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super.readExternal(element)
        val preferedUnityInstanceName = JDOMExternalizerUtil.readField(element, "PreferedUnityInstanceName")
        if (preferedUnityInstanceName != null)
            this.preferedUnityInstanceName = preferedUnityInstanceName
    }
}
