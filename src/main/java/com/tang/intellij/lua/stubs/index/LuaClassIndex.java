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

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 *
 * Created by tangzx on 2016/11/28.
 */
public class LuaClassIndex extends StringStubIndexExtension<LuaDocClassDef> {

    public static final StubIndexKey<String, LuaDocClassDef> KEY = StubIndexKey.createIndexKey("lua.index.class");

    private static final LuaClassIndex INSTANCE = new LuaClassIndex();

    public static LuaClassIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() { return LuaLanguage.INDEX_VERSION;}

    @NotNull
    @Override
    public StubIndexKey<String, LuaDocClassDef> getKey() {
        return KEY;
    }

    @Override
    public Collection<LuaDocClassDef> get(@NotNull String s, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(KEY, s, project, scope, LuaDocClassDef.class);
    }

    @Nullable
    public static LuaDocClassDef find(@NotNull String name, SearchContext context) {
        if (context.isDumb())
            return null;
        Collection<LuaDocClassDef> list = getInstance().get(name, context.getProject(), context.getScope());
        if (!list.isEmpty()) return list.iterator().next();
        return null;
    }
}
