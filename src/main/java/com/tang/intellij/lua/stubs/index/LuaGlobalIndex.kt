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

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import com.tang.intellij.lua.psi.LuaPsiElement
import com.tang.intellij.lua.search.SearchContext
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 *
 * Created by tangzx on 2017/1/16.
 */
class LuaGlobalIndex : StringStubIndexExtension<LuaPsiElement>() {

    override fun getKey(): StubIndexKey<String, LuaPsiElement> {
        return KEY
    }

    companion object {

        val KEY = StubIndexKey.createIndexKey<String, LuaPsiElement>("lua.index.global.var")

        val instance = LuaGlobalIndex()

        fun process(key: String, context: SearchContext, processor: Processor<LuaPsiElement>): Boolean {
            if (context.isDumb)
                return false
            return StubIndex.getInstance().processElements(KEY, key, context.project, context.getScope(), null, LuaPsiElement::class.java, processor)
        }

        fun find(key: String, context: SearchContext): LuaPsiElement? {
            var result: LuaPsiElement? = null
            process(key, context, Processor {
                result = it
                false
            })
            return result
        }

        fun findAll(key: String, context: SearchContext): Collection<LuaPsiElement> {
            val vars = SmartList<LuaPsiElement>()
            if (!context.isDumb) {
                StubIndex.getInstance().processElements(KEY, key, context.project, context.getScope(), LuaPsiElement::class.java) { s ->
                    vars.add(s)
                    true
                }
            }
            return vars
        }
    }
}
