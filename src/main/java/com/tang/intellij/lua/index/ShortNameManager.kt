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

package com.tang.intellij.lua.index

import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.psi.search.LuaShortNamesManager
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITyClass
import com.tang.intellij.lua.ty.TyClass
import com.tang.intellij.lua.ty.TySerializedClass

class ShortNameManager : LuaShortNamesManager() {

    override fun findMember(type: ITyClass, fieldName: String, context: SearchContext): LuaClassMember? {
        val hashCode = "${type.className}*$fieldName".hashCode()
        val all = ClassMemberIndex.instance.get(hashCode, context.project, context.scope)
        return all.firstOrNull()
    }

    override fun getClassMembers(clazzName: String, context: SearchContext): Collection<LuaClassMember> {
        val hashCode = clazzName.hashCode()
        val all = ClassMemberIndex.instance.get(hashCode, context.project, context.scope)
        return all
    }

    override fun processMembers(
        type: ITyClass,
        context: SearchContext,
        processor: Processor<LuaClassMember>
    ): Boolean {
        val hashCode = type.className.hashCode()
        val all = ClassMemberIndex.instance.get(hashCode, context.project, context.scope)
        if (!ContainerUtil.process(all, processor))
            return false

        // from supper
        return TyClass.processSuperClass(type, context) {
            processMembers(it, context, processor)
        }
    }

    override fun processMembers(
        className: String,
        fieldName: String,
        context: SearchContext,
        processor: Processor<LuaClassMember>,
        visitSuper: Boolean
    ): Boolean {
        val hashCode = "${className}*$fieldName".hashCode()
        val all = ClassMemberIndex.instance.get(hashCode, context.project, context.scope)
        if (!ContainerUtil.process(all, processor))
            return false

        if (visitSuper) {
            val ty = TySerializedClass(className)
            // from supper
            return TyClass.processSuperClass(ty, context) {
                processMembers(it, fieldName, context, processor)
            }
        }

        return true
    }
}