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

package com.tang.intellij.lua.debugger;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.tang.intellij.lua.debugger.app.LuaAppConfigurationType;
import com.tang.intellij.lua.debugger.app.LuaAppRunConfiguration;
import com.tang.intellij.lua.lang.LuaFileType;
import com.tang.intellij.lua.psi.LuaFile;
import com.tang.intellij.lua.psi.LuaFileUtil;

/**
 * Supports creating run configurations from context (by right-clicking a code element in the source editor or the project view).
 * Created by tangzx on 2017/6/3.
 */
public class LuaRunConfigurationProducer extends RunConfigurationProducer<LuaAppRunConfiguration> {
    public LuaRunConfigurationProducer() {
        super(LuaAppConfigurationType.getInstance());
    }

    @Override
    protected boolean setupConfigurationFromContext(LuaAppRunConfiguration luaAppRunConfiguration, ConfigurationContext configurationContext, Ref<PsiElement> ref) {
        PsiElement element = ref.get();
        PsiFile containingFile = element.getContainingFile();
        if (!(containingFile instanceof LuaFile))
            return false;

        luaAppRunConfiguration.setFile(LuaFileUtil.getShortUrl(element.getProject(), containingFile.getVirtualFile()));
        luaAppRunConfiguration.setName(containingFile.getName());
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(LuaAppRunConfiguration luaAppRunConfiguration, ConfigurationContext configurationContext) {
        PsiElement element = configurationContext.getPsiLocation();
        if (element == null)
            return false;
        PsiFile psiFile = element.getContainingFile();
        if (psiFile == null || psiFile.getFileType() != LuaFileType.INSTANCE)
            return false;
        VirtualFile file = luaAppRunConfiguration.getVirtualFile();
        return psiFile.getVirtualFile().equals(file);
    }
}
