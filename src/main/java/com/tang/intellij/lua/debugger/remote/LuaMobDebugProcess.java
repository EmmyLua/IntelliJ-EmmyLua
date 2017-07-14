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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.tang.intellij.lua.debugger.IRemoteConfiguration;
import com.tang.intellij.lua.debugger.LuaDebugProcess;
import com.tang.intellij.lua.debugger.LuaDebuggerEditorsProvider;
import com.tang.intellij.lua.debugger.remote.commands.DebugCommand;
import com.tang.intellij.lua.debugger.remote.commands.GetStackCommand;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaMobDebugProcess extends LuaDebugProcess implements MobServerListener {

    private IRemoteConfiguration runProfile;
    private LuaDebuggerEditorsProvider editorsProvider;
    private MobServer mobServer;

    protected LuaMobDebugProcess(@NotNull XDebugSession session) {
        super(session);
        current = this;
        runProfile = (IRemoteConfiguration) session.getRunProfile();
        editorsProvider = new LuaDebuggerEditorsProvider();
        mobServer = new MobServer(this);
    }

    private static LuaMobDebugProcess current;

    public static LuaMobDebugProcess getCurrent() {
        return current;
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return editorsProvider;
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();

        try {
            println("Start mobdebug server at port:" + runProfile.getPort());
            println("Waiting for process connection...");
            mobServer.start(runProfile.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (mobServer != null) {
            mobServer.stop();
        }
    }

    @Override
    protected void run() {
        mobServer.addCommand("RUN");
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        mobServer.addCommand("OVER");
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        mobServer.addCommand("STEP");
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        mobServer.addCommand("OUT");
    }

    @Override
    protected void registerBreakpoint(@NotNull XSourcePosition sourcePosition, @NotNull XLineBreakpoint breakpoint) {
        super.registerBreakpoint(sourcePosition, breakpoint);
        Project project = getSession().getProject();
        VirtualFile file = sourcePosition.getFile();
        String fileShortUrl = LuaFileUtil.getShortUrl(project, file);
        if (fileShortUrl != null) {
            mobServer.sendAddBreakpoint(fileShortUrl, sourcePosition.getLine() + 1);
            String extension = file.getExtension();
            if (extension != null) {
                fileShortUrl = fileShortUrl.substring(0, fileShortUrl.length() - extension.length() - 1);
                mobServer.sendAddBreakpoint(fileShortUrl, sourcePosition.getLine() + 1);
            }
        }
    }

    @Override
    protected void unregisterBreakpoint(@NotNull XSourcePosition sourcePosition, @NotNull XLineBreakpoint position) {
        super.unregisterBreakpoint(sourcePosition, position);
        VirtualFile file = sourcePosition.getFile();
        String fileShortUrl = LuaFileUtil.getShortUrl(getSession().getProject(), file);
        mobServer.sendRemoveBreakpoint(fileShortUrl, sourcePosition.getLine() + 1);
        String extension = file.getExtension();
        if (extension != null) {
            fileShortUrl = fileShortUrl.substring(0, fileShortUrl.length() - extension.length() - 1);
            mobServer.sendRemoveBreakpoint(fileShortUrl, sourcePosition.getLine() + 1);
        }
    }

    @Override
    public void handleResp(int code, String data) {
        switch (code) {
            case 202:
                runCommand(new GetStackCommand());
                break;
        }
    }

    @Override
    public void onSocketClosed() {
        getSession().stop();
    }

    @NotNull
    @Override
    public LuaMobDebugProcess getProcess() {
        return this;
    }

    public void runCommand(DebugCommand command) {
        mobServer.addCommand(command);
    }
}
