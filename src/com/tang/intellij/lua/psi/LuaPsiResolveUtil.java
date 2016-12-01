package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.sun.istack.internal.NotNull;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.doc.psi.LuaDocParamDef;
import com.tang.intellij.lua.doc.psi.api.LuaComment;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaPsiResolveUtil {

    public static PsiElement resolve(LuaNameRef ref) {
        String refName = ref.getText();
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
        for (LuaNameDef nameDef : body.getParDefList()) {
            if (nameDef.getName().equals(searchName)) {
                return nameDef;
            }
        }
        return null;
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

    static LuaDocClassDef resolveType(LuaNameDef nameDef) {
        if (nameDef instanceof LuaParDef) {
            LuaCommentOwner owner = PsiTreeUtil.getParentOfType(nameDef, LuaCommentOwner.class);
            if (owner != null) {
                LuaComment comment = owner.getComment();
                if (comment != null) {
                    LuaDocParamDef paramDef = comment.getParamDef(nameDef.getText());
                    if (paramDef != null) {
                        return paramDef.resolveType();
                    }
                }
            }
        } else {
            LuaCommentOwner owner = PsiTreeUtil.getParentOfType(nameDef, LuaCommentOwner.class);
            if (owner != null) {
                LuaComment comment = owner.getComment();
                if (comment != null) {
                    return comment.getClassDef();
                }
            }
        }
        return null;
    }
}
