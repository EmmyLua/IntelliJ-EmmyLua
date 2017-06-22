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

package com.tang.intellij.lua.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.lang.LuaLanguage;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaElementFactory {
    public static LuaFile createFile(Project project, String content) {
        String name = "dummy.lua";
        return (LuaFile) PsiFileFactory.getInstance(project).
                createFileFromText(name, LuaLanguage.INSTANCE, content);
    }

    @NotNull
    public static PsiElement createIdentifier(Project project, String name) {
        String content = "local " + name + " = 0";
        LuaFile file = createFile(project, content);
        LuaNameDef def = PsiTreeUtil.findChildOfType(file, LuaNameDef.class);
        assert (def != null);
        return def.getFirstChild();
    }

    public static LuaLiteralExpr createLiteral(Project project, String value) {
        String content = "local a = " + value;
        LuaFile file = createFile(project, content);
        return PsiTreeUtil.findChildOfType(file, LuaLiteralExpr.class);
    }

    public static PsiElement createName(Project project, String name) {
        PsiElement element = createWith(project, name + " = 1");

        return PsiTreeUtil.findChildOfType(element, LuaNameExpr.class);
    }

    @NotNull
    public static PsiElement newLine(Project project) {
        return createWith(project, "\n");
    }

    @NotNull
    public static PsiElement createWith(Project project, String code) {
        LuaFile file = createFile(project, code);
        return file.getFirstChild();
    }
}
