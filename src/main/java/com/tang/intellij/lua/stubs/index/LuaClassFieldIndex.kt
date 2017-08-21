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
import com.tang.intellij.lua.Constants
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

        fun find(key: String, context: SearchContext): LuaClassField? {
            if (context.isDumb)
                return null

            val list = instance.get(key, context.project, context.getScope())
            return if (!list.isEmpty()) list.iterator().next() else null
        }

        fun find(className: String, fieldName: String, context: SearchContext): MutableCollection<LuaClassField> {
            if (context.isDumb)
                return mutableListOf()

            val key = className + "*" + fieldName
            var list = instance.get(key, context.project, context.getScope())

            if (!list.isEmpty())
                return list

            // from supper
            val classDef = LuaClassIndex.find(className, context)
            if (classDef != null) {
                val type = classDef.classType
                val superClassName = type.superClassName
                if (superClassName != null) {
                    list = find(superClassName, fieldName, context)
                    if (!list.isEmpty())
                        return list
                }
            }

            return list
        }

        fun find(type: ITyClass, fieldName: String, context: SearchContext): LuaClassField? {
            val fields = findAll(type, fieldName, context)
            var perfect: LuaClassField? = null
            for (field in fields) {
                perfect = field
                if (field is LuaDocFieldDef)
                    break
            }
            return perfect
        }

        fun findAll(type: ITyClass, fieldName: String, context: SearchContext): Collection<LuaClassField> {
            var fields = find(type.className, fieldName, context)
            if (fields.isEmpty()) {
                type.lazyInit(context)
                if (type.aliasName != null)
                    fields = find(type.aliasName!!, fieldName, context)
            }
            return fields
        }

        fun findGlobal(name: String, context: SearchContext): Collection<LuaClassField> {
            return find(Constants.WORD_G, name, context)
        }
    }
}
