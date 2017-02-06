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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/2/6.
 */
public class StdSDK implements ApplicationComponent {
    private static String NAME = "Lua";

    @Override
    public void initComponent() {
        ProjectJdkTable pjt = ProjectJdkTable.getInstance();
        Sdk mySdk = pjt.findJdk(StdSDK.NAME);
        if (mySdk == null) {
            ProjectJdkImpl sdk = new ProjectJdkImpl(StdSDK.NAME, LuaSdkType.getInstance());
            SdkModificator sdkModificator = sdk.getSdkModificator();

            VirtualFile dir  = LuaFileUtil.getPluginVirtualDirectory();
            if (dir != null) {
                dir = dir.findChild("classes");
                if (dir != null) {
                    VirtualFile library = dir.findChild("std");
                    sdkModificator.addRoot(library, OrderRootType.CLASSES);
                }
            }

            sdkModificator.commitChanges();

            ApplicationManager.getApplication().runWriteAction(() -> ProjectJdkTable.getInstance().addJdk(sdk));
        }
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "StdSDK";
    }
}
