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
                LuaNameDef funcName = localFuncDef.getNameDef();
                //名字部分
                if (funcName != null && refName.equals(funcName.getText()))
                    return localFuncDef.getNameDef();
                //参数部分
                PsiElement result = resolveInFuncBody(localFuncDef.getFuncBody(), refName);
                if (result != null) return result;
            }
            else if (curr instanceof LuaGlobalFuncDef) {
                //参数部分
                LuaGlobalFuncDef globalFuncDef = (LuaGlobalFuncDef) curr;
                PsiElement result = resolveInFuncBody(globalFuncDef.getFuncBody(), refName);
                if (result != null) return result;
            }
        } while (!(curr instanceof PsiFile));
        return null;
    }

    static PsiElement resolveInFuncBody(LuaFuncBody body, String searchName) {
        if (body == null) return null;
        return resolveInParList(body.getParList(), searchName);
    }

    static PsiElement resolveInParList(LuaParList parList, String searchName) {
        if (parList == null) return null;
        return resolveInNameList(parList.getNameList(), searchName);
    }

    static PsiElement resolveInNameList(@NotNull LuaNameList nameList, String searchName) {
        if (nameList == null) return null;
        for (LuaNameDef nameDef : nameList.getNameDefList()) {
            if (nameDef.getName().equals(searchName)) {
                return nameDef;
            }
        }
        return null;
    }
}
