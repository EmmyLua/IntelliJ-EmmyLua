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

    public static PsiElement createLineBreak(Project project) {
        String content = "\n";
        LuaFile file = createFile(project, content);
        return file.getFirstChild();
    }

    public static PsiElement createParamDoc(Project project, String name) {
        String content = String.format("---@param %s #table", name);
        LuaFile file = createFile(project, content);
        return file.getFirstChild();
    }
}
