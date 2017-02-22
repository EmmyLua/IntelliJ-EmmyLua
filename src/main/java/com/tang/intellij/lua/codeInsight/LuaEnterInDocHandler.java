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

package com.tang.intellij.lua.codeInsight;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.psi.LuaDocTypes;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * LuaEnterInDocHandler
 * Created by tangzx on 2017/2/19.
 */
public class LuaEnterInDocHandler implements EnterHandlerDelegate {
    @Override
    public Result preprocessEnter(@NotNull PsiFile psiFile,
                                  @NotNull Editor editor,
                                  @NotNull Ref<Integer> caretOffsetRef,
                                  @NotNull Ref<Integer> caretAdvance,
                                  @NotNull DataContext dataContext,
                                  @Nullable EditorActionHandler editorActionHandler) {
        int caretOffset = caretOffsetRef.get();

        //inside comment
        LuaComment comment = PsiTreeUtil.findElementOfClassAtOffset(psiFile, caretOffset - 1, LuaComment.class, false);
        if (comment != null && caretOffset > comment.getTextOffset()) {
            ASTNode[] children = comment.getNode().getChildren(TokenSet.create(LuaDocTypes.DASHES));
            ASTNode last = children[children.length - 1];
            if (caretOffset > last.getStartOffset()) //在最后一个 --- 之前才有效
                return null;

            Document document = editor.getDocument();

            document.insertString(caretOffset, "\n---");
            editor.getCaretModel().moveToOffset(caretOffset + 4);

            Project project = comment.getProject();
            final TextRange textRange = comment.getTextRange();
            PsiDocumentManager.getInstance(project).commitDocument(document);
            ApplicationManager.getApplication().runWriteAction(() -> {
                CodeStyleManager styleManager = CodeStyleManager.getInstance(project);
                styleManager.adjustLineIndent(psiFile, textRange);
            });
            return Result.Stop;
        }
        return Result.Continue;
    }

    @Override
    public Result postProcessEnter(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull DataContext dataContext) {
        return null;
    }
}
