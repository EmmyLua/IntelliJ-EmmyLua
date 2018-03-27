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

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.tang.intellij.lua.lang.LuaIcons
import org.jetbrains.jps.model.module.JpsModuleSourceRootType
import javax.swing.Icon

/**
 *
 * Created by tangzx on 2016/12/24.
 */
class LuaModuleType : ModuleType<LuaModuleBuilder>(MODULE_TYPE) {

    // IDEA-171
    val bigIcon: Icon
        get() = LuaIcons.MODULE

    override fun createModuleBuilder() = LuaModuleBuilder()

    override fun getName() = "Lua"

    override fun getDescription() = "Lua module"

    override fun getNodeIcon(b: Boolean): Icon = LuaIcons.MODULE

    override fun isMarkInnerSupportedFor(type: JpsModuleSourceRootType<*>?) = true

    companion object {
        const val MODULE_TYPE = "LUA_MODULE"

        val instance: LuaModuleType
            get() = ModuleTypeManager.getInstance().findByID(MODULE_TYPE) as LuaModuleType
    }
}
