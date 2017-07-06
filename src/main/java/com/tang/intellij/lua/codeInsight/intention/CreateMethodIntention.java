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
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.LuaPredefinedScope;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * 创建方法
 * Created by TangZX on 2017/4/13.
 */
public class CreateMethodIntention extends BaseIntentionAction {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Create method";
    }

    @NotNull
    @Override
    public String getText() {
        return getFamilyName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        LuaCallExpr callExpr = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.getCaretModel().getOffset(), LuaCallExpr.class, false);
        if (callExpr != null && !callExpr.isFunctionCall()) {
            LuaFuncBodyOwner bodyOwner = callExpr.resolveFuncBodyOwner(new SearchContext(project));
            return bodyOwner == null;
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        LuaCallExpr callExpr = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.getCaretModel().getOffset(), LuaCallExpr.class, false);
        if (callExpr != null && !callExpr.isFunctionCall()) {
            LuaExpr expr = callExpr.getExpr();
            if (expr instanceof LuaIndexExpr) {
                LuaIndexExpr indexExpr = (LuaIndexExpr) expr;
                LuaTypeSet typeSet = indexExpr.guessPrefixType(new SearchContext(project));
                if (typeSet == null || typeSet.isEmpty()) return;

                InsertPosition position = calcInsertPosition(typeSet.getPerfect(), project);
                if (position != null) {
                    editor.getCaretModel().moveToOffset(position.offset);

                    TemplateManager manager = TemplateManager.getInstance(project);
                    Template template = manager.createTemplate("", "", String.format("\n\nfunction %s:$NAME$()\n$END$\nend", position.perfix));

                    template.addVariable("NAME", null, new TextExpression(indexExpr.getName()), true);
                    manager.startTemplate(editor, template);
                }
            }
        }
    }

    private static class InsertPosition {
        int offset;
        String perfix;
    }

    @Nullable
    private InsertPosition calcInsertPosition(LuaType perfect, Project project) {
        Collection<LuaClassMethodDef> methods = LuaClassMethodIndex.getInstance().get(perfect.getClassName(),
                project,
                new LuaPredefinedScope(project));
        if (!methods.isEmpty()) {
            LuaClassMethodDef methodDef = methods.iterator().next();
            LuaExpr expr = methodDef.getClassMethodName().getExpr();
            TextRange textRange = methodDef.getTextRange();
            InsertPosition position = new InsertPosition();
            position.offset = textRange.getEndOffset();
            position.perfix = expr.getText();
            return position;
        }
        return null;
    }
}
