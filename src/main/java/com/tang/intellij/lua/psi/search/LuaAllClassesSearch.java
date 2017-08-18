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
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ExtensibleQueryFactory;
import com.intellij.util.Query;
import com.tang.intellij.lua.ty.ITyClass;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/3/29.
 */
public class LuaAllClassesSearch extends ExtensibleQueryFactory<ITyClass, LuaAllClassesSearch.SearchParameters> {

    private static LuaAllClassesSearch INSTANCE = new LuaAllClassesSearch();

    public static class SearchParameters {

        private SearchScope searchScope;
        private Project project;

        SearchParameters(SearchScope searchScope, Project project) {
            this.searchScope = searchScope;

            this.project = project;
        }

        public Project getProject() {
            return project;
        }

        public SearchScope getSearchScope() {
            return searchScope;
        }
    }

    private LuaAllClassesSearch() {
        super("com.tang.intellij.lua");
    }

    public static Query<ITyClass> search(@NotNull SearchScope searchScope, @NotNull Project project) {
        return INSTANCE.createUniqueResultsQuery(new SearchParameters(searchScope, project));
    }
}
