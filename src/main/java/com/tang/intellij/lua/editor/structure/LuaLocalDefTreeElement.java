package com.tang.intellij.lua.editor.structure;

import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaLocalDef;

/**
 *
 * Created by TangZX on 2016/12/28.
 */
public class LuaLocalDefTreeElement extends LuaTreeElement<LuaLocalDef> {

    LuaLocalDefTreeElement(LuaLocalDef localDef) {
        super(localDef, LuaIcons.LOCAL_VAR);
    }

    @Override
    protected String getPresentableText() {
        PsiElement nameList = element.getNameList();
        if (nameList != null)
            return "local " + nameList.getText();
        return null;
    }
}
