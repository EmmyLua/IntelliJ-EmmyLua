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
import com.tang.intellij.lua.Constants;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaClassFieldIndex extends StringStubIndexExtension<LuaClassField> {

    public static final StubIndexKey<String, LuaClassField> KEY = StubIndexKey.createIndexKey("lua.index.class.field");

    private static final LuaClassFieldIndex INSTANCE = new LuaClassFieldIndex();

    public static LuaClassFieldIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() { return LuaLanguage.INDEX_VERSION;}

    @NotNull
    @Override
    public StubIndexKey<String, LuaClassField> getKey() {
        return KEY;
    }

    @Override
    public Collection<LuaClassField> get(@NotNull String s, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(KEY, s, project, scope, LuaClassField.class);
    }

    public static LuaClassField find(@NotNull String key, @NotNull SearchContext context) {
        if (context.isDumb())
            return null;

        Collection<LuaClassField> list = INSTANCE.get(key, context.getProject(), context.getScope());
        if (!list.isEmpty())
            return list.iterator().next();
        return null;
    }

    @NotNull
    public static Collection<LuaClassField> find(@NotNull String className, @NotNull String fieldName, @NotNull SearchContext context) {
        if (context.isDumb())
            return Collections.emptyList();

        String key = className + "." + fieldName;
        Collection<LuaClassField> list = INSTANCE.get(key, context.getProject(), context.getScope());

        if (!list.isEmpty())
            return list;

        // from supper
        LuaDocClassDef classDef = LuaClassIndex.find(className, context);
        if (classDef != null) {
            LuaType type = classDef.getClassType();
            String superClassName = type.getSuperClassName();
            if (superClassName != null) {
                list = find(superClassName, fieldName, context);
                if (!list.isEmpty())
                    return list;
            }
        }

        return list;
    }

    @Nullable
    public static LuaClassField find(LuaType type, String fieldName, SearchContext context) {
        Collection<LuaClassField> fields = findAll(type, fieldName, context);
        LuaClassField perfect = null;
        for (LuaClassField field : fields) {
            perfect = field;
            if (field instanceof LuaDocFieldDef)
                break;
        }
        return perfect;
    }

    @NotNull
    public static Collection<LuaClassField> findAll(LuaType type, String fieldName, SearchContext context) {
        Collection<LuaClassField> fields = find(type.getClassName(), fieldName, context);
        if (fields.isEmpty()) {
            type.initAliasName(context);
            if (type.getAliasName() != null)
                fields = find(type.getAliasName(), fieldName, context);
        }
        return fields;
    }

    public static Collection<LuaClassField> findGlobal(String name, SearchContext context) {
        return find(Constants.WORD_G, name, context);
    }
}
