package com.tang.intellij.lua.editor.structure;

import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassField;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaClassFieldTreeElement extends LuaTreeElement<LuaClassField> {
    private String fieldName;

    LuaClassFieldTreeElement(LuaClassField fieldDef) {
        super(fieldDef, LuaIcons.CLASS_FIELD);
        fieldName = fieldDef.getName();
        if (fieldName == null)
            fieldName = "unknown";
    }

    @Override
    protected String getPresentableText() {
        return  fieldName;
    }
}
