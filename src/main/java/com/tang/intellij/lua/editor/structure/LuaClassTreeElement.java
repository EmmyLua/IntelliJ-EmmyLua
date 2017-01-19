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

import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/13.
 */
public class LuaClassTreeElement extends LuaTreeElement<LuaDocClassDef> {

    private String className;

    LuaClassTreeElement(LuaDocClassDef docClassDef) {
        super(docClassDef, LuaIcons.CLASS);
        this.className = docClassDef.getName();
    }

    @Override
    protected String getPresentableText() {
        return className;
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        List<TreeElement> list = new ArrayList<>();
        Project project = element.getProject();
        ProjectAndLibrariesScope scope = new ProjectAndLibrariesScope(project);

        Collection<LuaClassField> classFieldDefs =
                LuaClassFieldIndex.getInstance().get(className, project, scope);
        for (LuaClassField classFieldDef : classFieldDefs) {
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
