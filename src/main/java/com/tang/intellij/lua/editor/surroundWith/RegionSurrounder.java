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

package com.tang.intellij.lua.editor.surroundWith;

import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * region surrounder
 * Created by tangzx on 2017/2/25.
 */
public class RegionSurrounder implements Surrounder {
    @Override
    public String getTemplateDescription() {
        return "Region";
    }

    @Override
    public boolean isApplicable(@NotNull PsiElement[] psiElements) {
        return true;
    }

    @Nullable
    @Override
    public TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement[] psiElements) throws IncorrectOperationException {

        PsiElement last = psiElements[psiElements.length - 1];
        TextRange lastTextRange = last.getTextRange();

        PsiElement first = psiElements[0];
        TextRange firstTextRange = first.getTextRange();

        final Document document = editor.getDocument();
        final int startLineNumber = document.getLineNumber(firstTextRange.getStartOffset());
        final String startIndent = document.getText(new TextRange(document.getLineStartOffset(startLineNumber), firstTextRange.getStartOffset()));

        String endString = "\n" + startIndent + "--endregion";
        String startString = "--region description\n" + startIndent;
        document.insertString(lastTextRange.getEndOffset(), endString);
        document.insertString(firstTextRange.getStartOffset(), startString);

        int begin = firstTextRange.getStartOffset() + 9;
        return new TextRange(begin, begin + 11);
    }
}
