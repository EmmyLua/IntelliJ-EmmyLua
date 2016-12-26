package com.tang.intellij.lua.editor.completion;

import com.tang.intellij.lua.psi.LuaFuncBody;
import com.tang.intellij.lua.psi.LuaParamNameDef;

import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/20.
 */
public class FuncInsertHandler extends ArgsInsertHandler {
    private LuaFuncBody funcBody;

    public FuncInsertHandler(LuaFuncBody funcBody) {

        this.funcBody = funcBody;
    }

    @Override
    protected List<LuaParamNameDef> getParams() {
        if (funcBody != null)
            return funcBody.getParamNameDefList();
        return null;
    }
}
