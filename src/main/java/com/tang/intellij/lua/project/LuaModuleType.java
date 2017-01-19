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

package com.tang.intellij.lua.project;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.tang.intellij.lua.lang.LuaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;

/**
 *
 * Created by tangzx on 2016/12/24.
 */
public class LuaModuleType extends ModuleType<LuaModuleBuilder> {
    private static final String MODULE_TYPE = "LUA_MODULE";

    public static LuaModuleType getInstance() {
        return (LuaModuleType) ModuleTypeManager.getInstance().findByID(MODULE_TYPE);
    }

    public LuaModuleType() {
        super(MODULE_TYPE);
    }

    @NotNull
    @Override
    public LuaModuleBuilder createModuleBuilder() {
        return new LuaModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "Lua";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Lua module";
    }

    @Override
    public Icon getBigIcon() {
        return LuaIcons.MODULE;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean b) {
        return LuaIcons.MODULE;
    }

    @Override
    public boolean isMarkInnerSupportedFor(JpsModuleSourceRootType type) {
        return true;
    }
}
