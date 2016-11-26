package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.sun.istack.internal.NotNull;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaPsiResolveUtil {

    public static PsiElement resolve(LuaNameRef ref) {
        String refName = ref.getId().getText();
        PsiElement curr = ref;
        do {
            PsiElement next = curr.getPrevSibling();
            if (next == null) {
                next = curr.getParent();
            }
            curr = next;

            if (curr instanceof LuaLocalDef) {
                LuaNameList nameList = ((LuaLocalDef) curr).getNameList();
                PsiElement result = resolveInNameList(nameList, refName);
                if (result != null) return result;
            }
            else if (curr instanceof LuaLocalFuncDef) {
                LuaLocalFuncDef localFuncDef = (LuaLocalFuncDef) curr;
                if (refName.equals(localFuncDef.getNameDef().getText()))
                    return localFuncDef.getNameDef();

                PsiElement result = resolveInParList(localFuncDef.getFuncBody().getParList(), refName);
                if (result != null) return result;
            }
        } while (!(curr instanceof PsiFile));
        return null;
    }

    static PsiElement resolveInParList(LuaParList parList, String searchName) {
        return resolveInNameList(parList.getNameList(), searchName);
    }

    static PsiElement resolveInNameList(@NotNull LuaNameList nameList, String searchName) {
        for (LuaNameDef nameDef : nameList.getNameDefList()) {
            if (nameDef.getName().equals(searchName)) {
                return nameDef;
            }
        }
        return null;
    }
}
