package com.tang.intellij.lua.lang.type;

import com.tang.intellij.lua.psi.LuaNameRef;

/**
 *
 * Created by tangzx on 2017/1/15.
 */
public class LuaGlobalType extends LuaType {

    public static LuaGlobalType create(LuaNameRef ref) {
        return new LuaGlobalType(ref);
    }

    private String clazzName;

    private LuaGlobalType(LuaNameRef ref) {
        super(ref);
        clazzName = ref.getText();
    }

    @Override
    public String getClassNameText() {
        return clazzName;
    }
}
