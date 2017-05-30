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

package com.tang.intellij.lua.debugger.remote;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.AbstractRunConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.tang.intellij.lua.debugger.IRemoteConfiguration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaMobConfiguration extends AbstractRunConfiguration implements IRemoteConfiguration {

    private int port = 8172;

    LuaMobConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory);
    }

    @Override
    public Collection<Module> getValidModules() {
        //final Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        final SettingsEditorGroup<LuaMobConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor("123", new LuaMobSettingsEditor());
        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new LuaCommandLineState(executionEnvironment);
    }

    public int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, "PORT", String.valueOf(port));
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        String port = JDOMExternalizerUtil.readField(element, "PORT");
        if (port != null)
            this.port = Integer.parseInt(port);
    }
}
