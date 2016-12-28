package com.tang.intellij.lua.editor.structure;

import com.intellij.icons.AllIcons;
import com.tang.intellij.lua.psi.LuaAssignStat;

/**
 *
 * Created by TangZX on 2016/12/28.
 */
public class LuaAssignTreeElement extends LuaTreeElement<LuaAssignStat> {
    LuaAssignTreeElement(LuaAssignStat target) {
        super(target, AllIcons.Nodes.ClassInitializer);
    }

    @Override
    protected String getPresentableText() {
        return element.getVarList().getText();
    }
}
