package com.tang.intellij.lua.lang.type;

import com.tang.intellij.lua.psi.LuaNameDef;
import com.tang.intellij.lua.psi.LuaPsiResolveUtil;

/**
 *
 * Created by tangzx on 2017/1/12.
 */
public class LuaAnonymousType extends LuaType {

    private final String clazzName;
    private LuaNameDef localDef;

    private LuaAnonymousType(LuaNameDef localDef) {
        super(localDef);
        this.localDef = localDef;
        this.clazzName = LuaPsiResolveUtil.getAnonymousType(localDef);
    }

    @Override
    public String getClassNameText() {
        return clazzName;
    }

    public static LuaAnonymousType create(LuaNameDef localDef) {
        return new LuaAnonymousType(localDef);
    }
}
