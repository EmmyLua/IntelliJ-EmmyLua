package com.tang.intellij.lua.lang.type;

import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import org.jetbrains.annotations.NotNull;

/**
 * class type
 * Created by tangzx on 2016/12/21.
 */
public class LuaClassType extends LuaType {

    public static LuaClassType create(@NotNull LuaDocClassDef def) {
        return new LuaClassType(def);
    }

    private LuaClassType(LuaDocClassDef classDef) {
        super(classDef);
        this.classDef = classDef;
    }

    private LuaDocClassDef classDef;
    private String className;
    private LuaType superType;

    public LuaType getSuperClass() {
        if (superType == null)
            superType = classDef.getSuperClass();
        return superType;
    }

    public String getClassNameText() {
        if (className == null)
            className = classDef.getName();
        return className;
    }
}
