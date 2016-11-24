package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaPsiUtil {

    public static PsiElement getUpperScope(PsiElement element) {
        PsiElement scope = element.getParent();
        while (scope != null) {
            if (scope instanceof PsiFile)
                return scope;
            if (scope instanceof LuaPsiScope)
                return scope;
            scope = scope.getParent();
        }
        assert false;
        return null;
    }

}
