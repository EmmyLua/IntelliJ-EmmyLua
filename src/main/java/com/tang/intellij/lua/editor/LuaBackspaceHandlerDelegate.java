package com.tang.intellij.lua.editor;

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.comment.psi.LuaDocTypes;

/**
 *
 * Created by tangzx on 2016/12/28.
 */
public class LuaBackspaceHandlerDelegate extends BackspaceHandlerDelegate {
    @Override
    public void beforeCharDeleted(char c, PsiFile psiFile, Editor editor) {

    }

    @Override
    public boolean charDeleted(char c, PsiFile psiFile, Editor editor) {
        if (c == '-') { // 一口气删了 ---
            int offset = editor.getCaretModel().getOffset();
            PsiElement element = psiFile.findElementAt(offset);
            if (element != null) {
                IElementType type = element.getNode().getElementType();
                if (type == LuaDocTypes.DASHES) {
                    int start = element.getTextOffset() - 1;
                    int end = element.getTextOffset() + element.getTextLength();
                    if (offset == end - 1) { //在 --- 最后面删的
                        editor.getDocument().deleteString(start, offset);
                        editor.getCaretModel().moveToOffset(start);
                    }
                }
            }
        }
        return false;
    }
}
