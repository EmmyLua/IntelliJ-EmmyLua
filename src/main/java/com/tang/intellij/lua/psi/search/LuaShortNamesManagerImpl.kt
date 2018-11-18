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
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassIndex

class LuaShortNamesManagerImpl : LuaShortNamesManager() {
    override fun findClass(name: String, project: Project, scope: GlobalSearchScope): LuaClass? {
        return LuaClassIndex.find(name, project, scope)
    }

    override fun findClass(name: String, context: SearchContext): LuaClass? {
        return LuaClassIndex.find(name, context)
    }

    override fun processAllClassNames(project: Project, processor: Processor<String>) {
        LuaClassIndex.processKeys(project, processor)
    }

    override fun processClassesWithName(name: String, project: Project, scope: GlobalSearchScope, processor: Processor<LuaClass>) {
        LuaClassIndex.process(name, project, scope, Processor { processor.process(it) })
    }
}