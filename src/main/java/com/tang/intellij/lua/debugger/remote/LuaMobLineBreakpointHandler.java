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

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.tang.intellij.lua.debugger.LuaLineBreakpointType;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/12/30.
 */
public class LuaMobLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {
    private LuaMobDebugProcess process;

    LuaMobLineBreakpointHandler(LuaMobDebugProcess process) {
        super(LuaLineBreakpointType.class);
        this.process = process;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
        process.addBreakpoint(xBreakpoint);
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> xBreakpoint, boolean b1) {
        process.removeBreakpoint(xBreakpoint);
    }
}
