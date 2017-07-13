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

package com.tang.intellij.lua.luacheck

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.util.treeView.AbstractTreeBuilder
import com.intellij.ide.util.treeView.AbstractTreeStructureBase
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.psi.PsiFile
import com.intellij.ui.AutoScrollToSourceHandler
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.HighlightableCellRenderer
import com.intellij.ui.components.JBScrollPane
import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeCellRenderer

/**
 * LuaCheckPanel
 * Created by tangzx on 2017/7/12.
 */
class LuaCheckPanel(val project: Project) : SimpleToolWindowPanel(false), DataProvider {
    val rootNode = DefaultMutableTreeNode()
    val treeModel = DefaultTreeModel(rootNode)
    var tree: JTree = JTree(treeModel)
    lateinit var builder:LuaCheckTreeBuilder

    override fun getActions(originalProvider: Boolean): MutableList<AnAction> {
        return super.getActions(originalProvider)
    }

    fun init() {
        createTree()
    }

    fun createTree() {
        tree.cellRenderer = NodeRenderer()
        tree.isRootVisible = false
        val jbScrollPane = JBScrollPane(tree)
        jbScrollPane.border = null
        setContent(jbScrollPane)

        val handler = object : AutoScrollToSourceHandler() {
            override fun isAutoScrollMode(): Boolean {
                return true
            }

            override fun setAutoScrollMode(p0: Boolean) {
            }
        }
        handler.install(tree)

        builder = buildTree(tree)
        builder.initRootNode()
    }

    fun buildTree(tree: JTree): LuaCheckTreeBuilder {
        return LuaCheckTreeBuilder(tree, treeModel, project)
    }

    override fun getData(dataId: String?): Any? {
        if (CommonDataKeys.NAVIGATABLE.`is`(dataId)) {
            val path = tree.selectionPath
            if (path == null) {
                return null
            } else {
                val node = path.lastPathComponent as DefaultMutableTreeNode
                val userObject = node.userObject
                if (userObject !is NodeDescriptor<*>) {
                    return null
                } else {
                    val element = userObject.element
                    if (element is LCRecord) {
                        return element.navigate(true)
                    }
                }
            }
        }
        return super.getData(dataId)
    }
}

class LuaCheckTreeBuilder(tree: JTree, model: DefaultTreeModel, val project: Project)
    : AbstractTreeBuilder(tree, model, LuaCheckTreeStructure(project), null, false) {

    override fun initRootNode() {
        super.initRootNode()
        performUpdate()
    }

    fun addFile(file: PsiFile): LCPsiFileNode {
        val root = rootElement as LCRootNode
        val fileNode = LCPsiFileNode(project, file)
        root.append(fileNode)
        return fileNode
    }

    fun addLCItem(item: LuaCheckRecordNodeData, fileNode: LCPsiFileNode) {
        fileNode.append(LCRecord(project, fileNode.value, item))
    }

    fun performUpdate() {
        treeModel?.reload()
        updater?.performUpdate()
    }
}

class LuaCheckTreeStructure(project: Project) : AbstractTreeStructureBase(project) {
    val root: LCRootNode = LCRootNode(project)

    override fun commit() { }

    override fun getProviders(): List<TreeStructureProvider> {
        return emptyList()
    }

    override fun getRootElement(): Any = root

    override fun hasSomethingToCommit(): Boolean = false
}

class LuaCheckCellRenderer : ColoredTreeCellRenderer() {
    override fun customizeCellRenderer(tree: JTree,
                                       value: Any,
                                       selected: Boolean,
                                       expanded: Boolean,
                                       leaf: Boolean,
                                       row: Int,
                                       hasFocus: Boolean) {
        val treeNode = value as DefaultMutableTreeNode
        val obj = treeNode.userObject
        when (obj) {
            is LuaCheckNodeData -> {
                obj.render(this)
            }
        }
    }

}

class LuaCheckCompositeRenderer : TreeCellRenderer {
    private val myNodeRenderer: NodeRenderer by lazy { NodeRenderer() }
    private val myColorTreeCellRenderer: HighlightableCellRenderer by lazy { HighlightableCellRenderer() }

    override fun getTreeCellRendererComponent(tree: JTree,
                                              value: Any,
                                              selected: Boolean,
                                              expanded: Boolean,
                                              leaf: Boolean,
                                              row: Int,
                                              hasFocus: Boolean): Component {
        val treeNode = value as DefaultMutableTreeNode
        val obj = treeNode.userObject
        when (obj) {
            is LuaCheckFileNodeData -> {

            }
        }
        return myNodeRenderer
    }
}