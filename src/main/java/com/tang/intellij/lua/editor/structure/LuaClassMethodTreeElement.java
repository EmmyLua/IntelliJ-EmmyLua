package com.tang.intellij.lua.editor.structure;

import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassMethodDef;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaClassMethodTreeElement extends LuaTreeElement<LuaClassMethodDef> {

    private String methodName;

    LuaClassMethodTreeElement(LuaClassMethodDef methodDef) {
        super(methodDef, LuaIcons.CLASS_METHOD);
        this.methodName = methodDef.getName() + methodDef.getParamFingerprint();
    }

    @Override
    protected String getPresentableText() {
        return methodName;
    }
}
