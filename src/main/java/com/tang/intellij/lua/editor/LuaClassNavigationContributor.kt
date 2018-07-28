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

package com.tang.intellij.lua.editor

import com.intellij.navigation.GotoClassContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassIndex

/**
 * Goto Class
 * Created by TangZX on 2016/12/12.
 */
class LuaClassNavigationContributor : GotoClassContributor {
    override fun getQualifiedName(navigationItem: NavigationItem): String? = null

    override fun getQualifiedNameSeparator(): String? {
        return "."
    }

    override fun getNames(project: Project, b: Boolean): Array<String> {
        val allClasses = LuaClassIndex.instance.getAllKeys(project)
        return allClasses.toTypedArray()
    }

    override fun getItemsByName(s: String, s1: String, project: Project, b: Boolean): Array<NavigationItem> {
        val classDef = LuaClassIndex.find(s, SearchContext(project))
        return if (classDef == null)
            emptyArray()
        else
            arrayOf(classDef)
    }
}
