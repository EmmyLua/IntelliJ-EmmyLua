package com.tang.intellij.lua.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.tang.intellij.lua.lang.LuaIcons;

/**
 *
 * Created by tangzx on 2016/12/24.
 */
public class CreateLuaFileAction extends CreateFileFromTemplateAction {
    private static final String CREATE_LUA_FILE = "New Lua File";

    public CreateLuaFileAction() {
        super(CREATE_LUA_FILE, "", LuaIcons.FILE);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(CREATE_LUA_FILE)
        .addKind("Source File", LuaIcons.FILE, "NewLua.lua");
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s, String s1) {
        return CREATE_LUA_FILE;
    }
}
