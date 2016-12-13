package com.tang.intellij.lua.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.comment.psi.LuaDocGlobalDef;
import com.tang.intellij.lua.comment.psi.LuaDocVisitor;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaFile;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.psi.LuaTypes;
import com.tang.intellij.lua.psi.LuaVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaFileTreeElement implements StructureViewTreeElement {

    private LuaFile file;

    public LuaFileTreeElement(LuaFile file) {

        this.file = file;
    }

    @Override
    public Object getValue() {
        return file;
    }

    @Override
    public void navigate(boolean b) {

    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return file.getName();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return file.getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.FILE;
            }
        };
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        List<TreeElement> list = new ArrayList<>();

        file.acceptChildren(new LuaVisitor(){

            public void visitDocComment(PsiElement comment) {
                comment.acceptChildren(new LuaDocVisitor(){
                    @Override
                    public void visitClassDef(@NotNull LuaDocClassDef o) {
                        String name = o.getClassNameText();
                        if (name != null) {
                            list.add(new LuaClassTreeElement(o));
                        }
                    }

                    @Override
                    public void visitGlobalDef(@NotNull LuaDocGlobalDef o) {

                    }

                    @Override
                    public void visitFieldDef(@NotNull LuaDocFieldDef o) {

                    }
                });
            }

            @Override
            public void visitGlobalFuncDef(@NotNull LuaGlobalFuncDef o) {
                list.add(new LuaGlobalFunctionTreeElement(o));
            }

            @Override
            public void visitElement(PsiElement element) {
                if (element.getNode().getElementType() == LuaTypes.DOC_COMMENT) {
                    visitDocComment(element);
                }
            }
        });

        return list.toArray(new TreeElement[list.size()]);
    }
}