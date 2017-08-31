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
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaClassMethod
import com.tang.intellij.lua.search.SearchContext

/**
 *
 * Created by tangzx on 2016/12/4.
 */
class LuaClassMethodIndex : StringStubIndexExtension<LuaClassMethod>() {

    override fun getVersion(): Int {
        return LuaLanguage.INDEX_VERSION
    }

    override fun getKey(): StubIndexKey<String, LuaClassMethod> {
        return KEY
    }

    override fun get(s: String, project: Project, scope: GlobalSearchScope): Collection<LuaClassMethod> {
        return StubIndex.getElements(KEY, s, project, scope, LuaClassMethod::class.java)
    }

    companion object {
        val KEY = StubIndexKey.createIndexKey<String, LuaClassMethod>("lua.index.class.method")

        val instance = LuaClassMethodIndex()

        fun findStaticMethods(className: String, context: SearchContext): Collection<LuaClassMethod> {
            val key = className + ".static"
            return instance.get(key, context.project, context.getScope())
        }

        fun process(key: String, context: SearchContext, processor: Processor<LuaClassMethod>): Boolean {
            if (context.isDumb)
                return false
            return StubIndex.getInstance().processElements(KEY, key, context.project, context.getScope(), null, LuaClassMethod::class.java, processor)
        }

        fun findStaticMethod(className: String, methodName: String, context: SearchContext): LuaClassMethod? {
            if (context.isDumb)
                return null

            val key = className + ".static." + methodName
            var result: LuaClassMethod? = null
            process(key, context, Processor {
                result = it
                false
            })
            return result
        }

        fun findMethodWithName(className: String, methodName: String, context: SearchContext): LuaClassMethod? {
            if (context.isDumb)
                return null
            var result: LuaClassMethod? = null
            process(className, context, Processor {
                if (methodName == it.name) {
                    result = it
                    return@Processor false
                }
                true
            })
            return result
        }
    }
}