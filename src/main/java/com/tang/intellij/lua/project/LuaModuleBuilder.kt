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

import com.intellij.ide.util.projectWizard.*
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.annotations.NonNls
import java.io.File
import java.util.*

/**
 * lua ModuleBuilder
 * Created by tangzx on 2016/12/24.
 */
class LuaModuleBuilder : ModuleBuilder(), SourcePathsBuilder {
    private var selectedSDK: Sdk? = null
    private var sourcePaths: MutableList<Pair<String, String>>? = null

    @Throws(ConfigurationException::class)
    override fun setupRootModel(rootModel: ModifiableRootModel) {
        if (selectedSDK != null)
            rootModel.sdk = selectedSDK
        else
            rootModel.inheritSdk()

        val contentEntry = doAddContentEntry(rootModel)
        if (contentEntry != null) {
            val sourcePaths = getSourcePaths()

            if (sourcePaths != null) {
                for (sourcePath in sourcePaths) {
                    val first = sourcePath.first
                    File(first).mkdirs()
                    val sourceRoot = LocalFileSystem.getInstance()
                            .refreshAndFindFileByPath(FileUtil.toSystemIndependentName(first))
                    if (sourceRoot != null) {
                        contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second)
                    }
                }
            }
        }
    }

    override fun getModuleType(): ModuleType<LuaModuleBuilder> = LuaModuleType.instance

    override fun modifyProjectTypeStep(settingsStep: SettingsStep): ModuleWizardStep? {
        return object : SdkSettingsStep(settingsStep, this, { sdkTypeId -> LuaSdkType.instance === sdkTypeId }) {

            override fun updateDataModel() {
                super.updateDataModel()
                selectedSDK = myJdkComboBox.selectedJdk
            }
        }
    }

    @Throws(ConfigurationException::class)
    override fun getSourcePaths(): List<Pair<String, String>>? {
        if (sourcePaths == null) {
            sourcePaths = ArrayList()
            val path = contentEntryPath + File.separator + "src"
            File(path).mkdirs()
            sourcePaths!!.add(Pair.create(path, ""))
        }
        return sourcePaths
    }

    override fun setSourcePaths(list: MutableList<Pair<String, String>>) {
        this.sourcePaths = list
    }

    override fun addSourcePath(pair: Pair<String, String>) {
        sourcePaths!!.add(pair)
    }
}
