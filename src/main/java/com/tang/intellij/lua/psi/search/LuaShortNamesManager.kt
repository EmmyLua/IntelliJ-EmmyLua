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

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.util.Processor
import com.tang.intellij.lua.comment.psi.LuaDocTagField
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITyClass
import com.tang.intellij.lua.ty.TyParameter

abstract class LuaShortNamesManager {
    companion object {
        val EP_NAME = ExtensionPointName.create<LuaShortNamesManager>("com.tang.intellij.lua.luaShortNamesManager")

        private val KEY = Key.create<LuaShortNamesManager>("com.tang.intellij.lua.luaShortNamesManager")

        fun getInstance(project: Project): LuaShortNamesManager {
            var instance = project.getUserData(KEY)
            if (instance == null) {
                instance = CompositeLuaShortNamesManager()
                project.putUserData(KEY, instance)
            }
            return instance
        }
    }

    open fun findClass(name: String, context: SearchContext): LuaClass? = null

    open fun findMember(type: ITyClass, fieldName: String, context: SearchContext): LuaClassMember? {
        var perfect: LuaClassMember? = null
        var tagField: LuaDocTagField? = null
        var tableField: LuaTableField? = null
        processAllMembers(type, fieldName, context) {
            when (it) {
                is LuaDocTagField -> {
                    tagField = it
                    false
                }

                is LuaTableField -> {
                    tableField = it
                    true
                }

                else -> {
                    if (perfect == null)
                        perfect = it
                    true
                }
            }
        }
        if (tagField != null) return tagField
        if (tableField != null) return tableField
        return perfect
    }

    open fun findMethod(
        className: String,
        methodName: String,
        context: SearchContext,
        visitSuper: Boolean = true
    ): LuaClassMethod? {
        var target: LuaClassMethod? = null
        processAllMembers(className, methodName, context, Processor {
            if (it is LuaClassMethod) {
                target = it
                return@Processor false
            }
            true
        }, visitSuper)
        return target
    }

    open fun processAllClassNames(project: Project, processor: Processor<String>): Boolean {
        return true
    }

    open fun processClassesWithName(name: String, context: SearchContext, processor: Processor<LuaClass>): Boolean {
        return true
    }

    open fun getClassMembers(clazzName: String, context: SearchContext): Collection<LuaClassMember> {
        return emptyList()
    }

    fun processAllMembers(
        type: ITyClass,
        memberName: String,
        context: SearchContext,
        processor: Processor<LuaClassMember>
    ): Boolean {
        return if (type is TyParameter)
            type.superClassName?.let { processAllMembers(it, memberName, context, processor) } ?: true
        else processAllMembers(type.className, memberName, context, processor)
    }

    open fun processAllMembers(
        className: String,
        fieldName: String,
        context: SearchContext,
        processor: Processor<LuaClassMember>,
        visitSuper: Boolean = true
    ): Boolean {
        return true
    }

    open fun processAllMembers(
        type: ITyClass,
        context: SearchContext,
        processor: Processor<LuaClassMember>
    ): Boolean {
        return true
    }

    open fun findAlias(name: String, context: SearchContext): LuaTypeAlias? = null

    open fun processAllAlias(project: Project, processor: Processor<String>): Boolean {
        return true
    }

    open fun findTypeDef(name: String, context: SearchContext): LuaTypeDef? {
        return findClass(name, context) ?: findAlias(name, context)
    }
}