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

import com.intellij.openapi.project.DumbService
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.stubs.index.LuaSuperClassIndex

/**
 * LuaClassInheritorsSearchExecutor
 * Created by tangzx on 2017/3/28.
 */
class LuaClassInheritorsSearchExecutor : QueryExecutor<LuaDocTagClass, LuaClassInheritorsSearch.SearchParameters> {

    private fun processInheritors(searchParameters: LuaClassInheritorsSearch.SearchParameters,
                                  typeName: String,
                                  processedNames: MutableSet<String>,
                                  processor: Processor<in LuaDocTagClass>): Boolean {
        var ret = true
        // recursion guard!!
        if (!processedNames.add(typeName))
            return ret

        val processed = mutableListOf<LuaDocTagClass>()
        LuaSuperClassIndex.process(typeName, searchParameters.project, searchParameters.searchScope, Processor {
            processed.add(it)
            ret = processor.process(it)
            ret
        })
        if (ret && searchParameters.isDeep) {
            for (def in processed) {
                ret = processInheritors(searchParameters, def.name, processedNames, processor)
                if (!ret) break
            }
        }
        return ret
    }

    override fun execute(searchParameters: LuaClassInheritorsSearch.SearchParameters, processor: Processor<in LuaDocTagClass>): Boolean {
        var ref = true
        DumbService.getInstance(searchParameters.project).runReadActionInSmartMode {
            ref = processInheritors(searchParameters, searchParameters.typeName, mutableSetOf(), processor)
        }
        return ref
    }
}
