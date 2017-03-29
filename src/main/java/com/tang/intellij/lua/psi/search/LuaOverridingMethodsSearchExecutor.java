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

package com.tang.intellij.lua.psi.search;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.QueryExecutor;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/3/29.
 */
public class LuaOverridingMethodsSearchExecutor implements QueryExecutor<LuaClassMethodDef, LuaOverridingMethodsSearch.SearchParameters> {
    @Override
    public boolean execute(@NotNull LuaOverridingMethodsSearch.SearchParameters searchParameters, @NotNull Processor<LuaClassMethodDef> processor) {
        LuaClassMethodDef method = searchParameters.getMethod();
        Project project = method.getProject();
        SearchContext context = new SearchContext(project);
        LuaType type = method.getClassType(context);
        String methodName = method.getName();
        if (type != null && methodName != null) {
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            Query<LuaDocClassDef> search = LuaClassInheritorsSearch.search(scope, project, type.getClassName());

            return search.forEach(luaClass -> {
                String name = luaClass.getName();
                if (name == null) return false;
                LuaClassMethodDef methodDef = LuaClassMethodIndex.findMethodWithName(name, methodName, context);
                return methodDef != null && processor.process(methodDef);
            });
        }
        return false;
    }
}
