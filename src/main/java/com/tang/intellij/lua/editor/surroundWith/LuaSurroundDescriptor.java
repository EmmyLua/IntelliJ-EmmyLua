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

import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaPsiStatement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * SurroundDescriptor
 * Created by tangzx on 2017/2/25.
 */
public class LuaSurroundDescriptor implements SurroundDescriptor {
    private Surrounder[] surrounders = new Surrounder[] {
            new RegionSurrounder()
    };

    @NotNull
    @Override
    public PsiElement[] getElementsToSurround(PsiFile psiFile, int startOffset, int endOffset) {
        return findStatementsInRange(psiFile, startOffset, endOffset);
    }

    @NotNull
    private static PsiElement[] findStatementsInRange(@NotNull PsiFile file, int startOffset, int endOffset) {
        FileViewProvider viewProvider = file.getViewProvider();
        PsiElement element1 = viewProvider.findElementAt(startOffset, LuaLanguage.INSTANCE);
        PsiElement element2 = viewProvider.findElementAt(endOffset - 1, LuaLanguage.INSTANCE);
        if (element1 instanceof PsiWhiteSpace) {
            startOffset = element1.getTextRange().getEndOffset();
            element1 = file.findElementAt(startOffset);
        }
        if (element2 instanceof PsiWhiteSpace) {
            endOffset = element2.getTextRange().getStartOffset();
            element2 = file.findElementAt(endOffset - 1);
        }
        if (element1 == null || element2 == null)
            return PsiElement.EMPTY_ARRAY;

        PsiElement parent = PsiTreeUtil.findCommonParent(element1, element2);
        if (parent == null)
            return PsiElement.EMPTY_ARRAY;
        while (true) {
            if (parent instanceof LuaPsiStatement) {
                if (!(element1 instanceof PsiComment)) {
                    parent = parent.getParent();
                }
                break;
            }
            //if (parent instanceof PsiCodeBlock) break;
            if (parent instanceof PsiCodeFragment) break;
            if (parent == null || parent instanceof PsiFile)
                return PsiElement.EMPTY_ARRAY;
            parent = parent.getParent();
        }

        if (!parent.equals(element1)) {
            while (!parent.equals(element1.getParent())) {
                element1 = element1.getParent();
            }
        }
        //if (startOffset != element1.getTextRange().getStartOffset())
        //    return PsiElement.EMPTY_ARRAY;

        if (!parent.equals(element2)) {
            while (!parent.equals(element2.getParent())) {
                element2 = element2.getParent();
            }
        }
        //if (endOffset != element2.getTextRange().getEndOffset())
        //    return PsiElement.EMPTY_ARRAY;

        /*if (parent instanceof PsiCodeBlock && parent.getParent() instanceof PsiBlockStatement &&
                element1 == ((PsiCodeBlock) parent).getLBrace() && element2 == ((PsiCodeBlock) parent).getRBrace()) {
            return new PsiElement[]{parent.getParent()};
        }*/

        PsiElement[] children = parent.getChildren();
        ArrayList<PsiElement> array = new ArrayList<>();
        boolean flag = false;
        for (PsiElement child : children) {
            if (child.equals(element1)) {
                flag = true;
            }
            if (flag && !(child instanceof PsiWhiteSpace)) {
                array.add(child);
            }
            if (child.equals(element2)) {
                break;
            }
        }

        for (PsiElement element : array) {
            if (!(element instanceof LuaPsiStatement || element instanceof PsiWhiteSpace || element instanceof PsiComment)) {
                return PsiElement.EMPTY_ARRAY;
            }
        }

        return PsiUtilCore.toPsiElementArray(array);
    }

    @NotNull
    @Override
    public Surrounder[] getSurrounders() {
        return surrounders;
    }

    @Override
    public boolean isExclusive() {
        return false;
    }
}