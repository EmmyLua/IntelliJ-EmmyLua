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
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.search.LuaPredefinedScope
import com.tang.intellij.lua.search.SearchContext

/**
 *
 * Created by tangzx on 2016/11/28.
 */
class LuaClassIndex : StringStubIndexExtension<LuaDocClassDef>() {

    override fun getVersion(): Int {
        return LuaLanguage.INDEX_VERSION
    }

    override fun getKey() = KEY

    companion object {

        val KEY = StubIndexKey.createIndexKey<String, LuaDocClassDef>("lua.index.class")

        val instance = LuaClassIndex()

        fun find(name: String, context: SearchContext): LuaDocClassDef? {
            if (context.isDumb)
                return null
            return find(name, context.project, context.getScope())
        }

        fun find(name: String, project: Project, scope: GlobalSearchScope): LuaDocClassDef? {
            var def: LuaDocClassDef? = null
            process(name, project, scope, Processor {
                def = it
                false
            })
            return def
        }

        fun process(key: String, project: Project, scope: GlobalSearchScope, processor: Processor<LuaDocClassDef>): Boolean {
            val collection = instance.get(key, project, scope)
            return ContainerUtil.process(collection, processor)
        }

        fun processKeys(project: Project, processor: Processor<String>) {
            val scope = LuaPredefinedScope(project)
            val allKeys = instance.getAllKeys(project)
            for (key in allKeys) {
                val ret = process(key, project, scope, Processor { false })
                if (!ret && !processor.process(key))
                    break
            }
        }
    }
}
