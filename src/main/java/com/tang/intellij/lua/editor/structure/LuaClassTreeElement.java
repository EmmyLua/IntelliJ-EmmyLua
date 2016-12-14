package com.tang.intellij.lua.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaClassTreeElement implements StructureViewTreeElement {

    private String className;
    private LuaDocClassDef docClassDef;

    public LuaClassTreeElement(LuaDocClassDef docClassDef) {
        this.className = docClassDef.getClassNameText();
        this.docClassDef = docClassDef;
    }

    @Override
    public Object getValue() {
        return docClassDef;
    }

    @Override
    public void navigate(boolean b) {
        docClassDef.navigate(b);
    }

    @Override
    public boolean canNavigate() {
        return docClassDef.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return docClassDef.canNavigateToSource();
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return className;
            }

            @Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.CLASS;
            }
        };
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        List<TreeElement> list = new ArrayList<>();
        Project project = docClassDef.getProject();
        ProjectAndLibrariesScope scope = new ProjectAndLibrariesScope(project);

        Collection<LuaDocFieldDef> classFieldDefs =
                LuaClassFieldIndex.getInstance().get(className, project, scope);
        for (LuaDocFieldDef classFieldDef : classFieldDefs) {
            list.add(new LuaClassFieldTreeElement(classFieldDef));
        }

        Collection<LuaClassMethodDef> classMethodDefs =
        LuaClassMethodIndex.getInstance().get(className, project, scope);
        for (LuaClassMethodDef methodDef : classMethodDefs) {
            list.add(new LuaClassMethodTreeElement(methodDef));
        }

        return list.toArray(new TreeElement[list.size()]);
    }
}
