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

package com.tang.intellij.lua.project.nodes

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesNode
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

class LuaProjectRootNode(project: Project, viewSettings: ViewSettings) : ProjectViewProjectNode(project, viewSettings) {
    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        if (myProject.isDisposed)
            return mutableListOf()
        val list = mutableListOf<AbstractTreeNode<*>>()

        val modules = ModuleManager.getInstance(myProject).modules
        for (module in modules) {
            list.add(LuaModuleNode(myProject, module, settings))
        }

        list.add(ExternalLibrariesNode(myProject, settings))
        return list
    }
}