package com.tang.intellij.lua.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaGlobalFunctionTreeElement implements StructureViewTreeElement {

    private String globalFuncName;
    private LuaGlobalFuncDef globalFuncDef;

    public LuaGlobalFunctionTreeElement(LuaGlobalFuncDef globalFuncDef) {
        globalFuncName = globalFuncDef.getName();
        this.globalFuncDef = globalFuncDef;
    }

    @Override
    public Object getValue() {
        return globalFuncDef;
    }

    @Override
    public void navigate(boolean b) {
        globalFuncDef.navigate(b);
    }

    @Override
    public boolean canNavigate() {
        return globalFuncDef.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return globalFuncDef.canNavigateToSource();
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return globalFuncName;
            }

            @Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.GLOBAL_FUNCTION;
            }
        };
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        return new TreeElement[0];
    }
}
