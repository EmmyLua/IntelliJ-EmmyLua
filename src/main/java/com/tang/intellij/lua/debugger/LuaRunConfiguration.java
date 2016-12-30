package com.tang.intellij.lua.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.AbstractRunConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaRunConfiguration extends AbstractRunConfiguration {

    LuaRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory);
    }

    @Override
    public Collection<Module> getValidModules() {
        final Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        List<Module> list = new ArrayList<>();
        return list;
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        final SettingsEditorGroup<LuaRunConfiguration> group = new SettingsEditorGroup<>();
        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new LuaCommandLineState(executionEnvironment);
    }
}
