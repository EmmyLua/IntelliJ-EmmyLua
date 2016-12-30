package com.tang.intellij.lua.debugger;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaConfigurationFactory extends ConfigurationFactory {

    protected LuaConfigurationFactory(LuaConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new LuaRunConfiguration(project, this);
    }
}
