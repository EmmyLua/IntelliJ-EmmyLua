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

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.projectRoots.*
import com.tang.intellij.lua.lang.LuaIcons
import org.jdom.Element
import java.io.File
import javax.swing.Icon

/**
 *
 * Created by tangzx on 2016/12/24.
 */
class LuaSdkType : SdkType("Lua SDK") {

    override fun suggestHomePath() = PathEnvironmentVariableUtil.findInPath("lua")?.parent

    override fun isValidSdkHome(s: String) = true

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String): String {
        val file = File(sdkHome)
        return file.name
    }

    override fun getVersionString(sdk: Sdk) = "1.0"

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable? =
            null

    override fun getPresentableName() = "Lua SDK"

    override fun saveAdditionalData(sdkAdditionalData: SdkAdditionalData, element: Element) {

    }

    override fun getIcon(): Icon = LuaIcons.FILE

    override fun getIconForAddAction(): Icon = LuaIcons.FILE

    companion object {
        val instance: LuaSdkType
            get() = SdkType.findInstance(LuaSdkType::class.java)
    }
}
