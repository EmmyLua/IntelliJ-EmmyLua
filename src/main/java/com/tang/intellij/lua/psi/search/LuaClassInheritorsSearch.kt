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

package com.tang.intellij.lua.psi.search

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Processor
import com.intellij.util.Query
import com.tang.intellij.lua.comment.psi.LuaDocTagClass

/**
 *
 * Created by tangzx on 2017/3/28.
 */
class LuaClassInheritorsSearch private constructor() : ExtensibleQueryFactory<LuaDocTagClass, LuaClassInheritorsSearch.SearchParameters>("com.tang.intellij.lua") {

    class SearchParameters internal constructor(val searchScope: GlobalSearchScope, val project: Project, val typeName: String, val isDeep: Boolean)

    companion object {

        private val INSTANCE = LuaClassInheritorsSearch()

        @JvmOverloads
        fun search(searchScope: GlobalSearchScope, project: Project, typeName: String, deep: Boolean = true): Query<LuaDocTagClass> {
            val parameters = SearchParameters(searchScope, project, typeName, deep)
            return INSTANCE.createUniqueResultsQuery(parameters)
        }

        fun isClassInheritFrom(searchScope: GlobalSearchScope, project: Project, thiz: String, sup: String): Boolean {
            val query = search(searchScope, project, thiz)
            return !query.forEach(Processor {
                it.name != sup
            })
        }
    }
}
