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

import com.intellij.openapi.application.ApplicationManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by tangzx on 2017/1/1.
 */
public class EvaluatorCommand extends DefaultCommand {
    public interface Callback {
        void onResult(String data);
    }

    private final Callback callback;
    private int dataLen;

    private static String createExpr(String chunk, boolean getChildren) {
        String serFN = "local function se(o, children) " +
                "if type(o) == 'string' then return { nil, o, 'string' } " +
                "elseif type(o) == 'number' then return { nil, o, 'number' } " +
                "elseif type(o) == 'table' then if not children then return { nil, tostring(o), 'table' } end; " +
                    "local r = {} " +
                    "for k, v in pairs(o) do " +
                        "r[k] = { k, tostring(v), type(v) } " +
                    "end return r " +
                "elseif type(o) == 'function' then return { nil, tostring(o), 'function' } " +
                "end end ";
        String exec = String.format("local function exec() %s end local data = exec() return se(data, %b)", chunk, getChildren);
        return serFN + exec;
    }

    public EvaluatorCommand(String expr, boolean getChildren, Callback callback) {
        super("EXEC " + expr, 2);
        this.callback = callback;
    }

    @Override
    public int handle(String data) {
        if (dataLen != 0) {
            int index = data.indexOf("return _;end");
            if (index > 0) {
                handleLines++;
                dataLen = index + 12;
                final String res = data.substring(0, dataLen);
                ApplicationManager.getApplication().runReadAction(() -> callback.onResult(res));
                return dataLen;
            }
            return 0;
        }
        return super.handle(data);
    }

    @Override
    protected void handle(int index, String data) {
        Pattern pattern = Pattern.compile("\\d+[^\\d]+(\\d+)");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            dataLen = Integer.parseInt(matcher.group(1));
        }
    }
}
