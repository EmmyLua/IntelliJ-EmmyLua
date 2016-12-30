package com.tang.intellij.lua.debugger;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.tang.intellij.lua.lang.LuaIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaConfigurationType implements ConfigurationType {

    private final LuaConfigurationFactory factory = new LuaConfigurationFactory(this);

    public static LuaConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(LuaConfigurationType.class);
    }

    @Override
    public String getDisplayName() {
        return "Lua";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "Lua Remote Debugger";
    }

    @Override
    public Icon getIcon() {
        return LuaIcons.FILE;
    }

    @NotNull
    @Override
    public String getId() {
        return "lua";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[] { factory };
    }
}
