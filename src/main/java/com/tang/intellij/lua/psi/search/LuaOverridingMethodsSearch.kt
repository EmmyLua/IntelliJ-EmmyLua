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

import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Query
import com.tang.intellij.lua.psi.LuaClassMethod

/**
 *
 * Created by tangzx on 2017/3/29.
 */
class LuaOverridingMethodsSearch private constructor() : ExtensibleQueryFactory<LuaClassMethod, LuaOverridingMethodsSearch.SearchParameters>("com.tang.intellij.lua") {

    class SearchParameters internal constructor(val method: LuaClassMethod, val isDeep: Boolean)

    companion object {
        private val INSTANCE = LuaOverridingMethodsSearch()

        @JvmOverloads
        fun search(methodDef: LuaClassMethod, deep: Boolean = true): Query<LuaClassMethod> =
                INSTANCE.createUniqueResultsQuery(SearchParameters(methodDef, deep))
    }
}
