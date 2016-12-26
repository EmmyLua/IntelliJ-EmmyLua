package com.tang.intellij.lua.project;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.tang.intellij.lua.lang.LuaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;

/**
 *
 * Created by tangzx on 2016/12/24.
 */
public class LuaModuleType extends ModuleType<LuaModuleBuilder> {
    private static final String MODULE_TYPE = "LUA_MODULE";

    public static LuaModuleType getInstance() {
        return (LuaModuleType) ModuleTypeManager.getInstance().findByID(MODULE_TYPE);
    }

    public LuaModuleType() {
        super(MODULE_TYPE);
    }

    @NotNull
    @Override
    public LuaModuleBuilder createModuleBuilder() {
        return new LuaModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "Lua";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Lua module";
    }

    @Override
    public Icon getBigIcon() {
        return LuaIcons.MODULE;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean b) {
        return LuaIcons.MODULE;
    }

    @Override
    public boolean isMarkInnerSupportedFor(JpsModuleSourceRootType type) {
        return true;
    }
}
