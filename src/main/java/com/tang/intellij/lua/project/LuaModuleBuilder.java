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

import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * lua ModuleBuilder
 * Created by tangzx on 2016/12/24.
 */
public class LuaModuleBuilder extends ModuleBuilder implements SourcePathsBuilder {
    private Sdk selectedSDK;
    private List<Pair<String, String>> sourcePaths;

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        if (selectedSDK != null)
            rootModel.setSdk(selectedSDK);
        else
            rootModel.inheritSdk();

        ContentEntry contentEntry = doAddContentEntry(rootModel);
        if (contentEntry != null) {
            final List<Pair<String,String>> sourcePaths = getSourcePaths();

            if (sourcePaths != null) {
                for (final Pair<String, String> sourcePath : sourcePaths) {
                    String first = sourcePath.first;
                    new File(first).mkdirs();
                    final VirtualFile sourceRoot = LocalFileSystem.getInstance()
                            .refreshAndFindFileByPath(FileUtil.toSystemIndependentName(first));
                    if (sourceRoot != null) {
                        contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second);
                    }
                }
            }
        }
    }

    @Override
    public ModuleType getModuleType() {
        return LuaModuleType.getInstance();
    }

    @Override
    public ModuleWizardStep modifyProjectTypeStep(@NotNull SettingsStep settingsStep) {
        return new SdkSettingsStep(settingsStep, this, sdkTypeId -> LuaSdkType.getInstance() == sdkTypeId) {

            @Override
            public void updateDataModel() {
                super.updateDataModel();
                LuaModuleBuilder.this.selectedSDK = myJdkComboBox.getSelectedJdk();
            }
        };
    }

    @Override
    public List<Pair<String, String>> getSourcePaths() throws ConfigurationException {
        if (sourcePaths == null) {
            sourcePaths = new ArrayList<>();
            @NonNls final String path = getContentEntryPath() + File.separator + "src";
            new File(path).mkdirs();
            sourcePaths.add(Pair.create(path, ""));
        }
        return sourcePaths;
    }

    @Override
    public void setSourcePaths(List<Pair<String, String>> list) {
        this.sourcePaths = list;
    }

    @Override
    public void addSourcePath(Pair<String, String> pair) {
        sourcePaths.add(pair);
    }
}
