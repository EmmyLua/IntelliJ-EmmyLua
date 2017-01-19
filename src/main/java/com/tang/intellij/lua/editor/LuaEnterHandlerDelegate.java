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

package com.tang.intellij.lua.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.editor.completion.KeywordInsertHandler;
import com.tang.intellij.lua.psi.LuaIndentRange;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 回车时的自动缩进
 * Created by tangzx on 2016/11/26.
 */
public class LuaEnterHandlerDelegate implements EnterHandlerDelegate {

    private static IElementType getEnd(IElementType range) {
        if (range == LuaTypes.TABLE_CONSTRUCTOR)
            return LuaTypes.RCURLY;
        if (range == LuaTypes.REPEAT_STAT)
            return LuaTypes.UNTIL;
        return LuaTypes.END;
    }

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
            editor.getDocument().insertString(caretOffset, "\n---");
            editor.getCaretModel().moveToOffset(caretOffset + 4);
            return Result.Stop;
        }

        PsiElement element = psiFile.findElementAt(caretOffset - 1);
        if (element != null) {
            boolean needAddEnd = false;
            PsiElement range = null;
            PsiElement cur = element;
            while (true) {
                PsiElement searched = cur.getParent();
                if (searched == null || searched instanceof PsiFile) break;
                if (searched instanceof LuaIndentRange) {
                    IElementType endType = getEnd(searched.getNode().getElementType());
                    PsiElement lastChild = searched.getLastChild();
                    if (lastChild.getNode().getElementType() != endType) {
                        needAddEnd = true;
                        range = searched;
                        break;
                    }
                }
                cur = searched;
            }

            if (needAddEnd) {
                IElementType endType = getEnd(range.getNode().getElementType());
                Document document = editor.getDocument();
                document.insertString(caretOffset, "\n" + endType.toString());
                Project project = element.getProject();

                final TextRange textRange = range.getTextRange();
                PsiDocumentManager.getInstance(project).commitDocument(document);
                ApplicationManager.getApplication().runWriteAction(()-> {
                    CodeStyleManager styleManager = CodeStyleManager.getInstance(project);
                    styleManager.adjustLineIndent(psiFile, textRange);
                    KeywordInsertHandler.autoIndent(endType, psiFile, project, document, caretOffset);
                });
                return Result.Stop;
            }
        }

        return Result.Continue;
    }

    @Override
    public Result postProcessEnter(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull DataContext dataContext) {
        return Result.Continue;
    }
}
