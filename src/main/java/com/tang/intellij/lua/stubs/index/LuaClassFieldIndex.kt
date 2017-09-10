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

package com.tang.intellij.lua.stubs.index

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITyClass

/**
 *
 * Created by tangzx on 2016/12/10.
 */
class LuaClassFieldIndex : StringStubIndexExtension<LuaClassField>() {

    override fun getVersion(): Int {
        return LuaLanguage.INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, LuaClassField> {
        return KEY
    }

    override fun get(s: String, project: Project, scope: GlobalSearchScope): MutableCollection<LuaClassField> {
        return StubIndex.getElements(KEY, s, project, scope, LuaClassField::class.java)
    }

    companion object {

        val KEY = StubIndexKey.createIndexKey<String, LuaClassField>("lua.index.class.field")

        val instance = LuaClassFieldIndex()

        fun process(key: String, context: SearchContext, processor: Processor<LuaClassField>): Boolean {
            if (context.isDumb)
                return false
            val all = instance.get(key, context.project, context.getScope())
            if (all.isEmpty()) return true
            @Suppress("LoopToCallChain")
            for (field in all) {
                if (!processor.process(field))
                    return false
            }
            return true
        }

        fun process(className: String, fieldName: String, context: SearchContext, processor: Processor<LuaClassField>): Boolean {
            val key = className + "*" + fieldName
            if (process(key, context, processor)) {
                // from supper
                val classDef = LuaClassIndex.find(className, context)
                if (classDef != null) {
                    val type = classDef.type
                    val superClassName = type.superClassName
                    if (superClassName != null) {
                        return process(superClassName, fieldName, context, processor)
                    }
                }
                return true
            }
            return false
        }

        fun find(type: ITyClass, fieldName: String, context: SearchContext): LuaClassField? {
            var perfect: LuaClassField? = null
            processAll(type, fieldName, context, Processor {
                perfect = it
                if (it is LuaDocFieldDef)
                    return@Processor false
                true
            })
            return perfect
        }

        fun processAll(type: ITyClass, fieldName: String, context: SearchContext, processor: Processor<LuaClassField>) {
            if (process(type.className, fieldName, context, processor)) {
                type.lazyInit(context)
                val alias = type.aliasName
                if (alias != null) {
                    process(alias, fieldName, context, processor)
                }
            }
        }
    }
}