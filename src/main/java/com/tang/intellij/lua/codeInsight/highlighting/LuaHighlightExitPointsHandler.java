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
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 *
 * Created by tangzx on 2017/3/18.
 */
public class LuaHighlightExitPointsHandler extends HighlightUsagesHandlerBase<PsiElement> {
    private LuaReturnStat target;
    private LuaFuncBody funcBody;

    LuaHighlightExitPointsHandler(@NotNull Editor editor, @NotNull PsiFile file, @NotNull LuaReturnStat psiElement, @NotNull LuaFuncBody funcBody) {
        super(editor, file);
        target = psiElement;
        this.funcBody = funcBody;
    }

    @Override
    public List<PsiElement> getTargets() {
        return Collections.singletonList(target);
    }

    @Override
    protected void selectTargets(List<PsiElement> list, Consumer<List<PsiElement>> consumer) {

    }

    @Override
    public void computeUsages(List<PsiElement> list) {
        if (funcBody != null) {
            funcBody.acceptChildren(new LuaVisitor() {
                @Override
                public void visitReturnStat(@NotNull LuaReturnStat o) {
                    addOccurrence(o);
                }

                @Override
                public void visitFuncBodyOwner(@NotNull LuaFuncBodyOwner o) {
                    // ignore sub function
                }

                @Override
                public void visitPsiElement(@NotNull LuaPsiElement o) {
                    o.acceptChildren(this);
                }
            });
        }
    }
}
