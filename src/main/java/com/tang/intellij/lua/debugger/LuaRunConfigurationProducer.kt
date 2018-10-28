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

package com.tang.intellij.lua.debugger

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.debugger.app.LuaAppConfigurationType
import com.tang.intellij.lua.debugger.app.LuaAppRunConfiguration
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.LuaFileUtil
import com.tang.intellij.lua.psi.LuaPsiFile

/**
 * Supports creating run configurations from context (by right-clicking a code element in the source editor or the project view).
 * Created by tangzx on 2017/6/3.
 */
class LuaRunConfigurationProducer : RunConfigurationProducer<LuaAppRunConfiguration>(LuaAppConfigurationType.getInstance()) {

    override fun setupConfigurationFromContext(luaAppRunConfiguration: LuaAppRunConfiguration, configurationContext: ConfigurationContext, ref: Ref<PsiElement>): Boolean {
        val element = ref.get()
        val containingFile = element.containingFile as? LuaPsiFile ?: return false

        luaAppRunConfiguration.file = LuaFileUtil.getShortPath(element.project, containingFile.virtualFile)
        luaAppRunConfiguration.name = containingFile.name

        val dir = containingFile.parent?.virtualFile
        if (dir != null) {
            val rootManager = ModuleRootManager.getInstance(configurationContext.module)
            for (root in rootManager.sourceRoots) {
                luaAppRunConfiguration.workingDir = root.canonicalPath
                return true
            }
            for (root in rootManager.contentRoots) {
                luaAppRunConfiguration.workingDir = root.canonicalPath
                return true
            }
        }
        return true
    }

    override fun isConfigurationFromContext(luaAppRunConfiguration: LuaAppRunConfiguration, configurationContext: ConfigurationContext): Boolean {
        val element = configurationContext.psiLocation ?: return false
        val psiFile = element.containingFile
        if (psiFile == null || psiFile.fileType !== LuaFileType.INSTANCE)
            return false
        val file = luaAppRunConfiguration.virtualFile
        return psiFile.virtualFile == file
    }
}
