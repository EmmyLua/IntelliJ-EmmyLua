package com.tang.intellij.lua.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 回车时的自动缩进
 * Created by tangzx on 2016/11/26.
 */
public class LuaEnterHandlerDelegate implements EnterHandlerDelegate {
    @Override
    public Result preprocessEnter(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull Ref<Integer> caretOffsetRef, @NotNull Ref<Integer> caretAdvance, @NotNull DataContext dataContext, @Nullable EditorActionHandler editorActionHandler) {
        int caretOffset = caretOffsetRef.get();
        PsiElement e1 = psiFile.findElementAt(caretOffset);

        return Result.Continue;
    }

    @Override
    public Result postProcessEnter(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull DataContext dataContext) {
        return Result.Continue;
    }
}
