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
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.tang.intellij.lua.psi.LuaClass
import com.tang.intellij.lua.search.SearchContext

abstract class LuaShortNamesManager {
    companion object {
        private val EP_NAME = ExtensionPointName.create<LuaShortNamesManager>("com.tang.intellij.lua.luaShortNamesManager")

        fun findClass(name: String, project: Project, scope: GlobalSearchScope): LuaClass? {
            for (ep in EP_NAME.extensions) {
                val c = ep.findClass(name, project, scope)
                if (c != null)
                    return c
            }
            return null
        }

        fun findClass(name: String, context: SearchContext): LuaClass? {
            for (ep in EP_NAME.extensions) {
                val c = ep.findClass(name, context)
                if (c != null)
                    return c
            }
            return null
        }

        fun processAllClassNames(project: Project, processor: Processor<String>) {
            for (ep in EP_NAME.extensions) {
                ep.processAllClassNames(project, processor)
            }
        }

        fun processClassesWithName(name: String, project: Project, scope: GlobalSearchScope, processor: Processor<LuaClass>) {
            for (ep in EP_NAME.extensions) {
                ep.processClassesWithName(name, project, scope, processor)
            }
        }
    }

    abstract fun findClass(name: String, context: SearchContext): LuaClass?

    abstract fun findClass(name: String, project: Project, scope: GlobalSearchScope): LuaClass?

    abstract fun processAllClassNames(project: Project, processor: Processor<String>)

    abstract fun processClassesWithName(name: String, project: Project, scope: GlobalSearchScope, processor: Processor<LuaClass>)
}