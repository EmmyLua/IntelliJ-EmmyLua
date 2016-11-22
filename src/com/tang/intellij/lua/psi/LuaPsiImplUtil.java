package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiReference;

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

    public static PsiReference getReference(LuaIdentifier identifier) {
        return null;
    }
}
