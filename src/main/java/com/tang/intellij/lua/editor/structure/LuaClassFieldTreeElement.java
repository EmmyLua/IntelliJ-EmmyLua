package com.tang.intellij.lua.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiNamedElement;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaClassFieldTreeElement implements StructureViewTreeElement {
    private LuaClassField fieldDef;
    private String fieldName;

    LuaClassFieldTreeElement(LuaClassField fieldDef) {
        PsiNamedElement nameDef = fieldDef.getNameDef();
        if (nameDef == null)
            fieldName = "unknown";
        else
            fieldName = nameDef.getName();
        this.fieldDef = fieldDef;
    }

    @Override
    public Object getValue() {
        return fieldDef;
    }

    @Override
    public void navigate(boolean b) {
        fieldDef.navigate(b);
    }

    @Override
    public boolean canNavigate() {
        return fieldDef.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return fieldDef.canNavigateToSource();
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return fieldName;
            }

            @Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.CLASS_FIELD;
            }
        };
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        return new TreeElement[0];
    }
}
