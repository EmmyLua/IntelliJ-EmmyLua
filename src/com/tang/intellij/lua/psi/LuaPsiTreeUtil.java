package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.ElementType;

/**
 *
 * Created by tangzx on 2016/12/3.
 */
public class LuaPsiTreeUtil {

    public static PsiElement getPrevSiblingSkipSpace(PsiElement element) {
        PsiElement prev = element.getPrevSibling();
        while (prev != null && prev.getNode().getElementType() == ElementType.WHITE_SPACE) {
            prev = prev.getPrevSibling();
        }
        return prev;
    }

    public static PsiElement getNextSiblingSkipSpace(PsiElement element) {
        PsiElement prev = element.getNextSibling();
        while (prev != null && prev.getNode().getElementType() == ElementType.WHITE_SPACE) {
            prev = prev.getNextSibling();
        }
        return prev;
    }

}
