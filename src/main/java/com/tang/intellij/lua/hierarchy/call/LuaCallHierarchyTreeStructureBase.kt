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

package com.tang.intellij.lua.hierarchy.call

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

abstract class LuaCallHierarchyTreeStructureBase(
        project: Project,
        element: PsiElement
) : HierarchyTreeStructure(project, LuaHierarchyNodeDescriptor(null, element, true)) {

    protected abstract fun getChildren(element: PsiElement): List<PsiElement>

    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<Any> {
        if (descriptor is LuaHierarchyNodeDescriptor) {
            val element = descriptor.psiElement
            val isCallable = LuaCallHierarchyUtil.isValidElement(element)
            val nodeDescriptor = baseDescriptor
            if (element == null || !isCallable || nodeDescriptor == null) {
                return emptyArray()
            }

            val children = getChildren(element)
            return children.distinct().map{LuaHierarchyNodeDescriptor(descriptor, it, false)}.toTypedArray()
        }
        return emptyArray()
    }

    override fun isAlwaysShowPlus(): Boolean {
        return true
    }
}