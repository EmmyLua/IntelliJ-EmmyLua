/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.editor.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.comment.psi.LuaDocGlobalDef;
import com.tang.intellij.lua.comment.psi.LuaDocVisitor;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaFileElement implements StructureViewTreeElement {

    private LuaFile file;

    public LuaFileElement(LuaFile file) {

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

            void visitDocComment(PsiElement comment) {
                comment.acceptChildren(new LuaDocVisitor(){
                    @Override
                    public void visitClassDef(@NotNull LuaDocClassDef o) {
                        String name = o.getName();
                        if (name != null) {
                            list.add(new LuaClassElement(o));
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
            public void visitAssignStat(@NotNull LuaAssignStat o) {
                super.visitAssignStat(o);
                list.add(new LuaAssignElement(o));
            }

            @Override
            public void visitGlobalFuncDef(@NotNull LuaGlobalFuncDef o) {
                list.add(new LuaGlobalFuncElement(o));
            }

            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof LuaCommentOwner) {
                    LuaCommentOwner owner = (LuaCommentOwner) element;
                    LuaComment comment = owner.getComment();
                    if (comment != null)
                        visitDocComment(comment);
                }
            }

            @Override
            public void visitLocalDef(@NotNull LuaLocalDef o) {
                LuaComment comment = o.getComment();
                if (comment != null)
                    visitDocComment(comment);
                else
                    list.add(new LuaLocalElement(o));
            }

            @Override
            public void visitLocalFuncDef(@NotNull LuaLocalFuncDef o) {
                list.add(new LuaLocalFuncElement(o));
            }

            @Override
            public void visitClassMethodDef(@NotNull LuaClassMethodDef o) {
                list.add(new LuaClassMethodElement(o));
            }
        });

        return list.toArray(new TreeElement[list.size()]);
    }
}