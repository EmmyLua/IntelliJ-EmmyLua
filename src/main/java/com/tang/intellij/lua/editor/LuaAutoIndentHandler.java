package com.tang.intellij.lua.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.editor.completion.KeywordInsertHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 当打出 then else elseif end 时自动缩进
 * Created by TangZX on 2016/12/20.
 */
public class LuaAutoIndentHandler extends TypedHandlerDelegate {
    @Override
    public Result charTyped(char c, Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        CaretModel caretModel = editor.getCaretModel();
        EditorEx ex = (EditorEx) editor;
        EditorHighlighter highlighter = ex.getHighlighter();
        HighlighterIterator iterator = highlighter.createIterator(caretModel.getOffset() - 1);

        IElementType type = iterator.getTokenType();

        KeywordInsertHandler.autoIndent(type, file, project, editor.getDocument(), caretModel.getOffset());

        return super.charTyped(c, project, editor, file);
    }
}
