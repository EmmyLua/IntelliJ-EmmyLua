package com.tang.intellij.lua.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaFileType extends LanguageFileType {

    public static final LuaFileType INSTANCE = new LuaFileType();

    protected LuaFileType() {
        super(LuaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Lua-Tang";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Lua language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "lua";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return LuaIcons.FILE;
    }
}
