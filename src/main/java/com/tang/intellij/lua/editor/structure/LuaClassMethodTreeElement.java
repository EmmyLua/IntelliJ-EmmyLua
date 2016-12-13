package com.tang.intellij.lua.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaClassMethodTreeElement implements StructureViewTreeElement {

    private String methodName;
    private LuaClassMethodDef methodDef;

    public LuaClassMethodTreeElement(LuaClassMethodDef methodDef) {
        this.methodName = methodDef.getMethodName();
        this.methodDef = methodDef;
    }

    @Override
    public Object getValue() {
        return methodDef;
    }

    @Override
    public void navigate(boolean b) {
        methodDef.navigate(b);
    }

    @Override
    public boolean canNavigate() {
        return methodDef.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return methodDef.canNavigateToSource();
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return methodName;
            }

            @Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.CLASS_METHOD;
            }
        };
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        return new TreeElement[0];
    }
}
