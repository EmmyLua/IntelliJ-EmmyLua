package com.tang.intellij.lua.editor.structure;

import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaGlobalFunctionTreeElement extends LuaTreeElement<LuaGlobalFuncDef> {

    private String globalFuncName;

    LuaGlobalFunctionTreeElement(LuaGlobalFuncDef globalFuncDef) {
        super(globalFuncDef, LuaIcons.GLOBAL_FUNCTION);
        globalFuncName = globalFuncDef.getName();
    }

    @Override
    protected String getPresentableText() {
        return globalFuncName;
    }
}
