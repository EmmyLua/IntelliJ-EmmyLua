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

import com.intellij.openapi.projectRoots.*;
import com.tang.intellij.lua.lang.LuaIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

/**
 *
 * Created by tangzx on 2016/12/24.
 */
public class LuaSdkType extends SdkType {
    public LuaSdkType() {
        super("Lua SDK");
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        return "C:/";
    }

    @Override
    public boolean isValidSdkHome(String s) {
        return true;
    }

    @Override
    public String suggestSdkName(@Nullable final String currentSdkName, @NotNull final String sdkHome) {
        File file = new File(sdkHome);
        return file.getName();
    }

    @Nullable
    @Override
    public String getVersionString(@NotNull Sdk sdk) {
        return "1.0";
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return null;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Lua SDK";
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData sdkAdditionalData, @NotNull Element element) {

    }

    @Override
    public Icon getIcon() {
        return LuaIcons.FILE;
    }

    @NotNull
    @Override
    public Icon getIconForAddAction() {
        return LuaIcons.FILE;
    }
}
