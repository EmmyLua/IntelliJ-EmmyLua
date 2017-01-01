package com.tang.intellij.lua.debugger.commands;

import com.intellij.openapi.application.ApplicationManager;

/**
 *
 * Created by tangzx on 2017/1/1.
 */
public class EvaluatorCommand extends DefaultCommand {
    public interface Callback {
        void onResult(String data);
    }

    private final Callback callback;

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
        super("EXEC " + createExpr(expr, getChildren), 2);
        this.callback = callback;
    }

    @Override
    protected void handle(int index, String data) {
        super.handle(index, data);
        if (index == 1) {
            System.out.println(data);
            ApplicationManager.getApplication().runReadAction(()-> callback.onResult(data));
        }
    }
}
