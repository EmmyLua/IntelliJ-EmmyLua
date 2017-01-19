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

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * Created by TangZX on 2017/1/19.
 */
public class LuaShortNameIndex extends StringStubIndexExtension<NavigatablePsiElement> {

    public static final StubIndexKey<String, NavigatablePsiElement> KEY = StubIndexKey.createIndexKey("lua.index.short_name");

    private static final LuaShortNameIndex INSTANCE = new LuaShortNameIndex();

    public static LuaShortNameIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() { return LuaLanguage.INDEX_VERSION;}

    @NotNull
    @Override
    public StubIndexKey<String, NavigatablePsiElement> getKey() {
        return KEY;
    }

    @NotNull
    public static Collection<NavigatablePsiElement> find(String key, SearchContext searchContext) {
        if (searchContext.isDumb())
            return Collections.emptyList();
        return INSTANCE.get(key, searchContext.getProject(), searchContext.getScope());
    }
}
