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
        processMembers(type, fieldName, context) {
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
        processMembers(className, methodName, context, Processor {
            if (it is LuaClassMethod) {
                target = it
                return@Processor false
            }
            true
        }, visitSuper)
        return target
    }

    @Deprecated("Use processClassNames instead.", replaceWith = ReplaceWith("processClassNames"))
    open fun processAllClassNames(project: Project, processor: Processor<String>): Boolean {
        return true
    }

    open fun processClassNames(project: Project, processor: Processor<String>): Boolean {
        return processAllClassNames(project, processor)
    }

    open fun processClassesWithName(name: String, context: SearchContext, processor: Processor<LuaClass>): Boolean {
        return true
    }

    open fun getClassMembers(clazzName: String, context: SearchContext): Collection<LuaClassMember> {
        return emptyList()
    }

    @Deprecated("Use processMembers instead.", replaceWith = ReplaceWith("processMembers"))
    open fun processAllMembers(
        type: ITyClass,
        memberName: String,
        context: SearchContext,
        processor: Processor<LuaClassMember>
    ): Boolean {
        return if (type is TyParameter)
            type.superClassName?.let { processMembers(it, memberName, context, processor) } ?: true
        else processMembers(type.className, memberName, context, processor)
    }

    open fun processMembers(
        type: ITyClass,
        memberName: String,
        context: SearchContext,
        processor: Processor<LuaClassMember>
    ): Boolean {
        return processAllMembers(type, memberName, context, processor)
    }

    open fun processMembers(
        className: String,
        fieldName: String,
        context: SearchContext,
        processor: Processor<LuaClassMember>,
        visitSuper: Boolean = true
    ): Boolean {
        return true
    }

    @Deprecated("Use processMembers instead.", replaceWith = ReplaceWith("processMembers"))
    open fun processAllMembers(
        type: ITyClass,
        context: SearchContext,
        processor: Processor<LuaClassMember>
    ): Boolean {
        return true
    }

    open fun processMembers(
        type: ITyClass,
        context: SearchContext,
        processor: Processor<LuaClassMember>
    ): Boolean {
        return processAllMembers(type, context, processor)
    }

    open fun findAlias(name: String, context: SearchContext): LuaTypeAlias? = null

    open fun processAllAlias(project: Project, processor: Processor<String>): Boolean {
        return true
    }

    open fun findTypeDef(name: String, context: SearchContext): LuaTypeDef? {
        return findClass(name, context) ?: findAlias(name, context)
    }
}