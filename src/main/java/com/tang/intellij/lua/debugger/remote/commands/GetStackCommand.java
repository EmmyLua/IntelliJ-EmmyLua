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

package com.tang.intellij.lua.debugger.remote.commands;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.tang.intellij.lua.debugger.LuaExecutionStack;
import com.tang.intellij.lua.debugger.remote.LuaStackFrame;
import com.tang.intellij.lua.debugger.remote.value.LuaRValue;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class GetStackCommand extends DefaultCommand {

    private boolean hasError;

    public GetStackCommand() {
        super("STACK", 1);
    }

    @Override
    protected void handle(int index, String data) {
        if (hasError) {
            new DefaultCommand("SUSPEND", 0).exec();
            debugProcess.setStack(new LuaExecutionStack(new ArrayList<>()));
            return;
        }

        if (data.startsWith("401")) {
            hasError = true;
            requireRespLines++;
            return;
        }

        Pattern pattern = Pattern.compile("(\\d+) (\\w+) (.+)");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            //String status = matcher.group(1);//200
            //String statusName = matcher.group(2);//OK
            String stackCode = matcher.group(3);
            Globals standardGlobals = JsePlatform.debugGlobals();
            LuaValue code = standardGlobals.load(stackCode);
            LuaFunction function = code.checkfunction();
            LuaValue value = function.call();

            ArrayList<XStackFrame> frames = new ArrayList<>();
            for (int i = 1; i <= value.length(); i++) {
                LuaValue stackValue = value.get(i);
                LuaValue stackInfo = stackValue.get(1);

                LuaValue funcName = stackInfo.get(1);
                LuaValue fileName = stackInfo.get(2);
                LuaValue line = stackInfo.get(4);

                XSourcePositionImpl position = null;
                VirtualFile virtualFile = LuaFileUtil.findFile(debugProcess.getSession().getProject(), fileName.toString());
                if (virtualFile != null) {
                    int nLine = line.toint();
                    position = XSourcePositionImpl.create(virtualFile, nLine - 1);
                }

                String functionName = funcName.toString();
                if (funcName.isnil())
                    functionName = "main";

                LuaStackFrame frame = new LuaStackFrame(functionName, position);

                parseValues(stackValue.get(2).checktable(), frame);
                parseValues(stackValue.get(3).checktable(), frame);

                frames.add(frame);
            }
            debugProcess.setStack(new LuaExecutionStack(frames));
        }
    }

    private void parseValues(LuaTable paramsTable, LuaStackFrame frame) {
        LuaValue[] keys = paramsTable.keys();
        for (LuaValue key : keys) {
            LuaValue luaValue = paramsTable.get(key);
            LuaValue desc = luaValue.get(2);
            LuaRValue xValue = LuaRValue.create(key.toString(), luaValue.get(1), desc.toString());
            frame.addValue(xValue);
        }
    }
}