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
import com.intellij.util.Processor
import com.tang.intellij.lua.psi.LuaClass
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITyClass

class CompositeLuaShortNamesManager : LuaShortNamesManager() {
    private val list: Array<LuaShortNamesManager> = LuaShortNamesManager.EP_NAME.extensions

    override fun findClass(name: String, project: Project, scope: GlobalSearchScope): LuaClass? {
        for (ep in list) {
            val c = ep.findClass(name, project, scope)
            if (c != null)
                return c
        }
        return null
    }

    override fun findClass(name: String, context: SearchContext): LuaClass? {
        for (ep in list) {
            val c = ep.findClass(name, context)
            if (c != null)
                return c
        }
        return null
    }

    override fun findMember(type: ITyClass, fieldName: String, context: SearchContext): LuaClassMember? {
        for (manager in list) {
            val ret = manager.findMember(type, fieldName, context)
            if (ret != null) return ret
        }
        return null
    }

    override fun processAllClassNames(project: Project, processor: Processor<String>) {
        for (ep in list) {
            ep.processAllClassNames(project, processor)
        }
    }

    override fun processClassesWithName(name: String, project: Project, scope: GlobalSearchScope, processor: Processor<LuaClass>) {
        for (ep in list) {
            ep.processClassesWithName(name, project, scope, processor)
        }
    }

    override fun getClassMembers(clazzName: String, project: Project, scope: GlobalSearchScope): MutableCollection<LuaClassMember> {
        var collection: MutableCollection<LuaClassMember>? = null
        for (manager in list) {
            val col = manager.getClassMembers(clazzName, project, scope)
            if (col.isNotEmpty()) {
                if (collection == null) collection = col else collection.addAll(col)
            }
        }
        return collection ?: mutableListOf()
    }
}