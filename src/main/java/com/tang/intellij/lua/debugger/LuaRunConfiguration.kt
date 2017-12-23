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

package com.tang.intellij.lua.debugger

import com.intellij.execution.configuration.AbstractRunConfiguration
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager

/**
 *
 * Created by tangzx on 2017/6/4.
 */
abstract class LuaRunConfiguration(project: Project, factory: ConfigurationFactory) : AbstractRunConfiguration(project, factory) {
    @Throws(RuntimeConfigurationException::class)
    protected fun checkSourceRoot() {
        var sourceRootExist = false
        val modules = ModuleManager.getInstance(project).modules
        for (module in modules) {
            val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots
            if (sourceRoots.isNotEmpty()) {
                sourceRootExist = true
                break
            }
        }

        if (!sourceRootExist) {
            throw RuntimeConfigurationError("Sources root not found.")
        }
    }

    open fun createCommandLine(): GeneralCommandLine? {
        return null
    }
}
