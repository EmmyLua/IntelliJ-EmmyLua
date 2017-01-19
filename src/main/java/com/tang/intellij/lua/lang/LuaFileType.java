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

package com.tang.intellij.lua.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaFileType extends LanguageFileType {

    public static final LuaFileType INSTANCE = new LuaFileType();

    protected LuaFileType() {
        super(LuaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "lua";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Lua language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "lua";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return LuaIcons.FILE;
    }
}
