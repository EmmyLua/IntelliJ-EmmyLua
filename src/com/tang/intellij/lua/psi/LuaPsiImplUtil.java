package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;

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
        identifier.getId().replace(newId);
        return newId;
    }

    public static String getName(LuaNameDef identifier) {
        return identifier.getId().getText();
    }
}
