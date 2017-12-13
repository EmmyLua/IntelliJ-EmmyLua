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

package com.tang.intellij.lua.project

import com.intellij.openapi.module.ModuleConfigurationEditor
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState
import java.util.*

/**
 *
 * Created by tangzx on 2016/12/24.
 */
class LuaModuleConfigurationEditorProvider : ModuleConfigurationEditorProvider {
    override fun createEditors(moduleConfigurationState: ModuleConfigurationState): Array<ModuleConfigurationEditor> {
        val editors = ArrayList<ModuleConfigurationEditor>()
        val module = moduleConfigurationState.rootModel.module
        val moduleType = ModuleType.get(module)
        if (moduleType == LuaModuleType.instance) {
            val factory = DefaultModuleConfigurationEditorFactory.getInstance()
            editors.add(factory.createModuleContentRootsEditor(moduleConfigurationState))
            editors.add(factory.createClasspathEditor(moduleConfigurationState))
        }
        return editors.toTypedArray()
    }
}
