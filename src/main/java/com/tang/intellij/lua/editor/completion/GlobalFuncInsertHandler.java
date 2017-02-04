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

package com.tang.intellij.lua.editor.completion;

import com.intellij.openapi.project.Project;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaGlobalFuncIndex;

/**
 *
 * Created by TangZX on 2016/12/20.
 */
public class GlobalFuncInsertHandler extends ArgsInsertHandler {
    private String globalFuncName;
    private Project project;

    GlobalFuncInsertHandler(String name, Project project) {
        globalFuncName = name;
        this.project = project;
    }

    @Override
    protected String[] getParams() {
        LuaGlobalFuncDef luaGlobalFuncDef = LuaGlobalFuncIndex.find(globalFuncName, new SearchContext(project));
        if (luaGlobalFuncDef != null) {
            return luaGlobalFuncDef.getParams();
        }
        return null;
    }
}
