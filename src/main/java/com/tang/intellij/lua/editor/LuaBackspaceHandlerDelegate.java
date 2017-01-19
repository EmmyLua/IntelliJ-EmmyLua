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
