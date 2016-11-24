package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaPsiResolveUtil {

    public static PsiElement resolve(LuaIdentifierRef ref) {
        PsiElement scope = LuaPsiUtil.getUpperScope(ref);
        PsiElement result = null;
        while (scope != null) {
            result = resolveInScope(scope, ref);
            if (result == null && !(scope instanceof PsiFile))
                scope = LuaPsiUtil.getUpperScope(scope);
            else
                break;
        }
        return result;
    }

    public static PsiElement resolveInScope(final PsiElement scope, final LuaRef reference) {
        String refName = reference.getRefName();
        PsiElement[] children = scope.getChildren();
        for (PsiElement child : children) {
            if (child instanceof LuaPsiScope) continue;

            if (child instanceof LuaLocalDef) {
                LuaNameList nameList = ((LuaLocalDef) child).getNameList();
                for (LuaNameDef nameDef : nameList.getNameDefList()) {
                    if (nameDef.getName().equals(refName)) {
                        return nameDef;
                    }
                }
            }
            else {
                PsiElement r = resolveInScope(child, reference);
                if (r != null) return r;
            }
        }
        return null;
    }

}
