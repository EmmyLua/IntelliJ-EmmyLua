package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.tang.intellij.lua.doc.LuaCommentUtil;
import com.tang.intellij.lua.doc.psi.LuaDocPsiImplUtil;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.reference.LuaNameReference;
import org.jetbrains.annotations.NotNull;

/**
 * LuaPsiImplUtil
 * Created by TangZX on 2016/11/22.
 */
public class LuaPsiImplUtil {

    public enum ArgType {
        NAME_LIST,
        TABLE,
        STRING
    }

    public static ArgType getArgType(LuaArgs args) {
        return ArgType.NAME_LIST;
    }

    public static PsiElement setName(LuaNameDef identifier, String name) {
        PsiElement newId = LuaElementFactory.createIdentifier(identifier.getProject(), name);
        PsiElement oldId = identifier.getFirstChild();
        oldId.replace(newId);
        return newId;
    }

    @NotNull
    public static String getName(LuaNameDef identifier) {
        return identifier.getText();
    }

    public static PsiReference getReference(LuaNameRef ref) {
        return new LuaNameReference(ref);
    }

    public static LuaComment getComment(LuaGlobalFuncDef globalFuncDef) {
        return LuaCommentUtil.findComment(globalFuncDef);
    }

    public static LuaComment getComment(LuaLocalFuncDef localFuncDef) {
        return LuaCommentUtil.findComment(localFuncDef);
    }

    public static LuaComment getComment(LuaLocalDef localDef) {
        return LuaCommentUtil.findComment(localDef);
    }
}
