package com.tang.intellij.lua.editor.structure;

import com.intellij.psi.PsiNamedElement;
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
        PsiNamedElement nameDef = fieldDef.getNameDef();
        if (nameDef == null)
            fieldName = "unknown";
        else
            fieldName = nameDef.getName();
    }

    @Override
    protected String getPresentableText() {
        return  fieldName;
    }
}
