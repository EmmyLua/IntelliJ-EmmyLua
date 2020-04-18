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

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.util.Key

/**
 * base configuration factory
 * disable `Make` task
 */
abstract class LuaConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun configureBeforeRunTaskDefaults(providerID: Key<out BeforeRunTask<*>>?, task: BeforeRunTask<*>?) {
        if ("Make" == providerID?.toString())
            task?.isEnabled = false
    }

    override fun getId(): String {
        return name
    }
}