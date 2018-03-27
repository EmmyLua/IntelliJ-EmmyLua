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

import com.intellij.ide.hierarchy.CallHierarchyBrowserBase
import com.intellij.ide.hierarchy.HierarchyBrowserManager
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.ide.hierarchy.call.CallHierarchyNodeDescriptor
import com.intellij.ide.util.treeView.AlphaComparator
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.ide.util.treeView.SourceComparator
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.psi.PsiElement
import com.intellij.ui.PopupHandler
import com.tang.intellij.lua.psi.LuaClassMethodDef
import com.tang.intellij.lua.psi.LuaFuncDef
import com.tang.intellij.lua.psi.LuaLocalFuncDef
import java.util.*
import javax.swing.JTree

class LuaCallHierarchyBrowser(element: PsiElement) : CallHierarchyBrowserBase(element.project, element) {
    companion object {
        private const val GROUP_LUA_CALL_HIERARCHY_POPUP = "LuaCallHierarchyPopupMenu"
    }

    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is LuaClassMethodDef || element is LuaFuncDef || element is LuaLocalFuncDef
    }

    override fun getComparator(): Comparator<NodeDescriptor<Any>>? {
        return if (HierarchyBrowserManager.getInstance(myProject).state!!.SORT_ALPHABETICALLY) {
            AlphaComparator.INSTANCE
        } else {
            SourceComparator.INSTANCE
        }
    }

    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        return (descriptor as? CallHierarchyNodeDescriptor)?.enclosingElement
    }


    private fun createHierarchyTree(group: ActionGroup): JTree {
        val tree = createTree(false)
        PopupHandler.installPopupHandler(tree, group, ActionPlaces.CALL_HIERARCHY_VIEW_POPUP, ActionManager.getInstance())
        return tree
    }

    override fun createTrees(trees: MutableMap<String, JTree>) {
        val group = ActionManager.getInstance().getAction(GROUP_LUA_CALL_HIERARCHY_POPUP) as ActionGroup

        val callerTree = createHierarchyTree(group)
        val calleeTree = createHierarchyTree(group)

        trees[CallHierarchyBrowserBase.CALLER_TYPE] = callerTree
        trees[CallHierarchyBrowserBase.CALLEE_TYPE] = calleeTree
    }

    override fun createHierarchyTreeStructure(typeName: String, psiElement: PsiElement): HierarchyTreeStructure? =
            when (typeName) {
                CallHierarchyBrowserBase.CALLER_TYPE -> LuaCallerFunctionTreeStructure(myProject, psiElement)
                CallHierarchyBrowserBase.CALLEE_TYPE -> LuaCalleeFunctionTreeStructure(myProject, psiElement)
                else -> null
            }
}