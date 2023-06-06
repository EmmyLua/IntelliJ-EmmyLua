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
import com.intellij.util.Processor
import com.tang.intellij.lua.psi.LuaClass
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.psi.LuaClassMethod
import com.tang.intellij.lua.psi.LuaTypeAlias
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITyClass

class CompositeLuaShortNamesManager : LuaShortNamesManager() {
    private val list: Array<LuaShortNamesManager> = LuaShortNamesManager.EP_NAME.extensions

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

    override fun findMethod(
        className: String,
        methodName: String,
        context: SearchContext,
        visitSuper: Boolean
    ): LuaClassMethod? {
        for (manager in list) {
            val ret = manager.findMethod(className, methodName, context, visitSuper)
            if (ret != null) return ret
        }
        return null
    }

    override fun processClassNames(project: Project, processor: Processor<String>): Boolean {
        for (ep in list) {
            if (!ep.processClassNames(project, processor))
                return false
        }
        return true
    }

    override fun processMembers(
        className: String,
        fieldName: String,
        context: SearchContext,
        processor: Processor<LuaClassMember>,
        visitSuper: Boolean
    ): Boolean {
        for (manager in list) {
            if (!manager.processMembers(className, fieldName, context, processor, visitSuper))
                return false
        }
        return true
    }

    override fun processMembers(
        type: ITyClass,
        context: SearchContext,
        processor: Processor<LuaClassMember>
    ): Boolean {
        for (manager in list) {
            if (!manager.processMembers(type, context, processor))
                return false
        }
        return true
    }

    override fun processClassesWithName(name: String, context: SearchContext, processor: Processor<LuaClass>): Boolean {
        for (ep in list) {
            if (!ep.processClassesWithName(name, context, processor))
                return false
        }
        return true
    }

    override fun getClassMembers(clazzName: String, context: SearchContext): Collection<LuaClassMember> {
        val collection = mutableListOf<LuaClassMember>()
        for (manager in list) {
            val col = manager.getClassMembers(clazzName, context)
            collection.addAll(col)
        }
        return collection
    }

    override fun findAlias(name: String, context: SearchContext): LuaTypeAlias? {
        for (manager in list) {
            val alias = manager.findAlias(name, context)
            if (alias != null)
                return alias
        }
        return null
    }

    override fun processAllAlias(project: Project, processor: Processor<String>): Boolean {
        for (manager in list) {
            if (!manager.processAllAlias(project, processor))
                return false
        }
        return true
    }
}