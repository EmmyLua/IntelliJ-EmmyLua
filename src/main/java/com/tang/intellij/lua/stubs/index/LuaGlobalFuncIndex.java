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

package com.tang.intellij.lua.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.search.SearchContext;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 *
 * Created by TangZX on 2016/11/22.
 */
public class LuaGlobalFuncIndex extends StringStubIndexExtension<LuaGlobalFuncDef> {

    public static final StubIndexKey<String, LuaGlobalFuncDef> KEY = StubIndexKey.createIndexKey("lua.index.global_function");

    private static final LuaGlobalFuncIndex INSTANCE = new LuaGlobalFuncIndex();

    public static LuaGlobalFuncIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() { return LuaLanguage.INDEX_VERSION;}

    @NotNull
    @Override
    public StubIndexKey<String, LuaGlobalFuncDef> getKey() {
        return KEY;
    }

    public static LuaGlobalFuncDef find(String key, SearchContext context) {
        if (context.isDumb())
            return null;

        Collection<LuaGlobalFuncDef> defs = new SmartList<>();
        StubIndex.getInstance().processElements(KEY, key, context.getProject(), context.getScope(), LuaGlobalFuncDef.class, (s) -> {
            defs.add(s);
            return true;
        });
        if (!defs.isEmpty()) {
            return defs.iterator().next();
        }
        return null;
    }
}
