package com.tang.intellij.lua.lang.type;

import com.tang.intellij.lua.doc.psi.LuaDocClassDef;

/**
 * 类型说明
 * Created by TangZX on 2016/12/4.
 */
public class LuaType {

    public static LuaType create(LuaDocClassDef classDef) {
        LuaType type = new LuaType();
        type.classDef = classDef;
        return type;
    }

    protected LuaType() {

    }

    private LuaDocClassDef classDef;

    public LuaDocClassDef getClassDef() {
        return classDef;
    }

    public String getClassNameText() {
        return classDef.getClassNameText();
    }
}
