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

package com.tang.intellij.lua.debugger.app;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.tang.intellij.lua.debugger.DebuggerType;
import com.tang.intellij.lua.debugger.IRemoteConfiguration;
import com.tang.intellij.lua.debugger.LuaRunConfiguration;
import com.tang.intellij.lua.debugger.LuaCommandLineState;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * Created by tangzx on 2017/5/7.
 */
public class LuaAppRunConfiguration extends LuaRunConfiguration implements IRemoteConfiguration {
    private String program = SystemInfoRt.isWindows ? "lua.exe" : "lua";
    private String file;
    private String workingDir;
    private DebuggerType debuggerType = DebuggerType.Attach;

    LuaAppRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory);
    }

    @Override
    public Collection<Module> getValidModules() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<LuaAppRunConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor("program", new LuaAppSettingsEditor(getProject()));
        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new LuaCommandLineState(executionEnvironment);
    }

    String getProgram() {
        return program;
    }

    void setProgram(String program) {
        this.program = program;
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, "program", program);
        JDOMExternalizerUtil.writeField(element, "file", file);
        JDOMExternalizerUtil.writeField(element, "workingDir", workingDir);
        JDOMExternalizerUtil.writeField(element, "debuggerType", String.valueOf(debuggerType.value()));
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        String program = JDOMExternalizerUtil.readField(element, "program");
        if (program != null)
            this.program = program;
        file = JDOMExternalizerUtil.readField(element, "file");
        workingDir = JDOMExternalizerUtil.readField(element, "workingDir");

        String debuggerType = JDOMExternalizerUtil.readField(element, "debuggerType");
        if (debuggerType != null)
            this.debuggerType = DebuggerType.valueOf(Integer.parseInt(debuggerType));
    }

    @Override
    public int getPort() {
        return 8172;
    }

    public String getFile() {
        return file;
    }

    @Nullable
    public VirtualFile getVirtualFile() {
        return LuaFileUtil.findFile(getProject(), file);
    }

    public void setFile(String file) {
        this.file = file;
    }

    public DebuggerType getDebuggerType() {
        return debuggerType;
    }

    public void setDebuggerType(DebuggerType debuggerType) {
        this.debuggerType = debuggerType;
    }

    public String getWorkingDir() {
        if (workingDir == null || workingDir.isEmpty())
            workingDir = getDefaultWorkingDir();
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getDefaultWorkingDir() {
        Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        for (Module module : modules) {
            VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            for (VirtualFile sourceRoot : sourceRoots) {
                String path = sourceRoot.getCanonicalPath();
                if (path != null) {
                    return path;
                }
            }
        }
        return null;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkConfiguration();
        String program = getProgram();
        if (program == null || program.isEmpty()) {
            throw new RuntimeConfigurationError("Program doesn't exist.");
        }

        String workingDir = getWorkingDir();
        if (workingDir == null || !new File(workingDir).exists()) {
            throw new RuntimeConfigurationError("Working dir doesn't exist.");
        }

        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile == null) {
            throw new RuntimeConfigurationError("Entry file doesn't exist.");
        }
    }

    @Override
    public GeneralCommandLine createCommandLine() {
        GeneralCommandLine commandLine = new GeneralCommandLine().withExePath(program);
        commandLine.addParameters(getFile());
        commandLine.setWorkDirectory(getWorkingDir());
        return commandLine;
    }
}
