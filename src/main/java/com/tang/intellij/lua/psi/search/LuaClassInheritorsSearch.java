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
import com.intellij.psi.search.searches.ExtensibleQueryFactory;
import com.intellij.util.Query;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/3/28.
 */
public class LuaClassInheritorsSearch extends ExtensibleQueryFactory<LuaDocClassDef, LuaClassInheritorsSearch.SearchParameters> {

    private static LuaClassInheritorsSearch INSTANCE = new LuaClassInheritorsSearch();

    public static class SearchParameters {

        private GlobalSearchScope searchScope;
        private Project project;
        private String typeName;

        SearchParameters(GlobalSearchScope searchScope, Project project, String typeName) {
            this.searchScope = searchScope;
            this.project = project;

            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }

        public GlobalSearchScope getSearchScope() {
            return searchScope;
        }

        public Project getProject() {
            return project;
        }
    }

    private LuaClassInheritorsSearch() {
        super("com.tang.intellij.lua");
    }

    public static Query<LuaDocClassDef> search(@NotNull GlobalSearchScope searchScope, @NotNull Project project, final String typeName) {
        SearchParameters parameters = new SearchParameters(searchScope, project, typeName);
        return INSTANCE.createUniqueResultsQuery(parameters);
    }
}
