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

import com.intellij.ide.util.treeView.AbstractTreeBuilder
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
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
class LuaCheckPanel : SimpleToolWindowPanel(false) {
    val rootNode = DefaultMutableTreeNode()
    val treeModel = DefaultTreeModel(rootNode)
    var tree: JTree = JTree(treeModel)

    override fun getActions(originalProvider: Boolean): MutableList<AnAction> {
        return super.getActions(originalProvider)
    }

    fun init() {
        createTree()
    }

    fun createTree() {
        tree.cellRenderer = LuaCheckCellRenderer()
        tree.isRootVisible = false
        val jbScrollPane = JBScrollPane(tree)
        jbScrollPane.border = null
        setContent(jbScrollPane)
    }

    fun buildTree(tree: JTree): LuaCheckTreeBuilder {
        return LuaCheckTreeBuilder()
    }

    fun clear() {
        rootNode.removeAllChildren()
    }

    fun addNode(nodeData: LuaCheckNodeData): DefaultMutableTreeNode {
        val node = DefaultMutableTreeNode(nodeData)
        rootNode.add(node)
        ApplicationManager.getApplication().invokeLater { treeModel.reload() }
        return node
    }

    fun addNode(nodeData: LuaCheckNodeData, parentNode: DefaultMutableTreeNode): DefaultMutableTreeNode {
        val node = DefaultMutableTreeNode(nodeData)
        parentNode.add(node)
        ApplicationManager.getApplication().invokeLater { treeModel.reload() }
        return node
    }
}

class LuaCheckTreeBuilder : AbstractTreeBuilder() {

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