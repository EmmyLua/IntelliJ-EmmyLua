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

package com.tang.intellij.lua.debugger.attach;

import com.intellij.execution.process.ProcessInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.xdebugger.attach.XLocalAttachGroup;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.LuaLanguage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 * Created by tangzx on 2017/2/28.
 */
public class LuaLocalAttachGroup implements XLocalAttachGroup {

    public static final LuaLocalAttachGroup INSTANCE = new LuaLocalAttachGroup();

    @Override
    public int getOrder() {
        return XLocalAttachGroup.DEFAULT.getOrder() - 10;
    }

    @NotNull
    @Override
    public String getGroupName() {
        return LuaLanguage.INSTANCE.getID();
    }

    @NotNull
    @Override
    public Icon getProcessIcon(@NotNull Project project, @NotNull ProcessInfo processInfo, @NotNull UserDataHolder userDataHolder) {
        return LuaIcons.FILE;
    }

    @NotNull
    @Override
    public String getProcessDisplayText(@NotNull Project project, @NotNull ProcessInfo processInfo, @NotNull UserDataHolder userDataHolder) {
        return processInfo.getExecutableName();
    }

    @Override
    public int compare(@NotNull Project project, @NotNull ProcessInfo a, @NotNull ProcessInfo b, @NotNull UserDataHolder userDataHolder) {
        return XLocalAttachGroup.DEFAULT.compare(project, a, b, userDataHolder);
    }
}
