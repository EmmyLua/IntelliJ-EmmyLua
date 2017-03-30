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

package com.tang.intellij.lua.codeInsight.highlighting;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.psi.LuaFuncBody;
import com.tang.intellij.lua.psi.LuaReturnStat;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2017/3/18.
 */
public class LuaHighlightUsagesHandlerFactory extends HighlightUsagesHandlerFactoryBase {
    @Nullable
    @Override
    public HighlightUsagesHandlerBase createHighlightUsagesHandler(@NotNull Editor editor,
                                                                   @NotNull PsiFile psiFile,
                                                                   @NotNull PsiElement psiElement) {
        if (psiElement.getNode().getElementType() == LuaTypes.RETURN) {
            LuaReturnStat returnStat = PsiTreeUtil.getParentOfType(psiElement, LuaReturnStat.class);
            if (returnStat != null) {
                LuaFuncBody funcBody = PsiTreeUtil.getParentOfType(returnStat, LuaFuncBody.class);
                if (funcBody != null) {
                    return new LuaHighlightExitPointsHandler(editor, psiFile, returnStat, funcBody);
                }
            }
        }
        return null;
    }
}
