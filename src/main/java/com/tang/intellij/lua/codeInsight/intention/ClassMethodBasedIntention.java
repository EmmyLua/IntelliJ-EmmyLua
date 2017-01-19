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

package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/12/16.
 */
public abstract class ClassMethodBasedIntention extends BaseIntentionAction {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "ClassMethodBasedIntention";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        LuaClassMethodDef classMethodDef = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.getCaretModel().getOffset(), LuaClassMethodDef.class, false);
        return classMethodDef != null && isAvailable(classMethodDef, editor);
    }

    protected boolean isAvailable(LuaClassMethodDef methodDef, Editor editor) {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        LuaClassMethodDef classMethodDef = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.getCaretModel().getOffset(), LuaClassMethodDef.class, false);
        invoke(classMethodDef, editor);
    }

    protected void invoke(LuaClassMethodDef methodDef, Editor editor) {

    }
}
