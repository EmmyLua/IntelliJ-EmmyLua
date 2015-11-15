package com.tang.intellij.lua.psi;

import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.lang.LuaLanguage;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaElementType extends IElementType {
    public LuaElementType(String debugName) {
        super(debugName, LuaLanguage.INSTANCE);
    }
}
