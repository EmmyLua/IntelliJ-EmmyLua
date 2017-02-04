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

package com.tang.intellij.lua.lang.type;

import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

/**
 * class type
 * Created by tangzx on 2016/12/21.
 */
public class LuaClassType extends LuaType {

    public static LuaClassType create(@NotNull LuaDocClassDef def) {
        return new LuaClassType(def);
    }

    private LuaClassType(LuaDocClassDef classDef) {
        super();
        this.classDef = classDef;
        this.clazzName = classDef.getName();
    }

    private LuaDocClassDef classDef;
    private LuaType superType;

    public LuaType getSuperClass(SearchContext context) {
        if (superType == null)
            superType = classDef.getSuperClass(context);
        return superType;
    }
}
