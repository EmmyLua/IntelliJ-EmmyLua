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

import com.intellij.find.FindModel
import com.intellij.find.impl.FindInProjectUtil
import com.intellij.icons.AllIcons
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.TreeExpander
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.util.treeView.AbstractTreeBuilder
import com.intellij.ide.util.treeView.AbstractTreeStructureBase
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.ui.AutoScrollToSourceHandler
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.usageView.UsageInfo
import com.intellij.usages.impl.UsagePreviewPanel
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.PlatformIcons
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * LuaCheckPanel
 * Created by tangzx on 2017/7/12.
 */
class LuaCheckPanel(val project: Project) : SimpleToolWindowPanel(false), DataProvider {
    private val rootNode = DefaultMutableTreeNode()
    private val treeModel = DefaultTreeModel(rootNode)
    private val tree: JTree = JTree(treeModel)
    val builder:LuaCheckTreeBuilder = LuaCheckTreeBuilder(tree, treeModel, project)
    private val treeExpander = MyTreeExpander()
    private var myUsagePreviewPanel: UsagePreviewPanel? = null

    inner class MyShowPackagesAction : ToggleAction("Group By Packages", null, PlatformIcons.GROUP_BY_PACKAGES) {
        override fun isSelected(actionEvent: AnActionEvent): Boolean {
            return builder.arePackagesShown
        }

        override fun setSelected(actionEvent: AnActionEvent, state: Boolean) {
            builder.arePackagesShown = state
        }
    }

    inner class MyPreviewAction internal constructor() : ToggleAction("Preview Source", null, AllIcons.Actions.PreviewDetails) {

        override fun isSelected(actionEvent: AnActionEvent): Boolean {
            return builder.showPreview
        }

        override fun setSelected(actionEvent: AnActionEvent, state: Boolean) {
            myUsagePreviewPanel?.isVisible = state
            builder.showPreview = state
            if (state) {
                updatePreview()
            }
        }
    }

    inner class MyTreeExpander : TreeExpander {
        override fun collapseAll() {
            builder.collapseAll()
        }

        override fun canExpand() = true

        override fun expandAll() {
            builder.expandAll()
        }

        override fun canCollapse() = true
    }

    inner class MyAutoScrollToSourceHandler : AutoScrollToSourceHandler() {
        override fun isAutoScrollMode() = builder.isAutoScrollMode

        override fun setAutoScrollMode(value: Boolean) {
            builder.isAutoScrollMode = value
        }
    }

    fun init() {
        tree.cellRenderer = NodeRenderer()
        tree.isRootVisible = false
        tree.selectionModel.addTreeSelectionListener {
            ApplicationManager.getApplication().invokeLater { updatePreview() }
        }

        EditSourceOnDoubleClickHandler.install(tree)
        //auto scroll source handler
        val handler = MyAutoScrollToSourceHandler()
        handler.install(tree)

        //toolbar
        val toolBarPanel = JPanel(GridLayout())
        setToolbar(toolBarPanel)
        val group = DefaultActionGroup()
        group.add(CommonActionsManager.getInstance().createExpandAllAction(treeExpander, this))
        group.add(CommonActionsManager.getInstance().createCollapseAllAction(treeExpander, this))
        group.add(MyShowPackagesAction())
        group.add(handler.createToggleAction())
        group.add(MyPreviewAction())
        toolBarPanel.add(ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, group, false).component)

        builder.initRootNode()

        //preview
        myUsagePreviewPanel = UsagePreviewPanel(project, FindInProjectUtil.setupViewPresentation(false, FindModel()))
        myUsagePreviewPanel?.isVisible = builder.showPreview

        setContent(createCenterComponent())
    }

    private fun createCenterComponent(): JComponent {
        val splitter = OnePixelSplitter()
        val jbScrollPane = JBScrollPane(tree)
        jbScrollPane.border = null
        splitter.firstComponent = jbScrollPane//ScrollPaneFactory.createScrollPane(tree, false)
        splitter.secondComponent = myUsagePreviewPanel
        return splitter
    }

    private fun updatePreview() {
        val treePath = tree.selectionPath
        treePath ?: return

        val list: MutableList<UsageInfo> = mutableListOf()
        val node = treePath.lastPathComponent as DefaultMutableTreeNode
        val userdata = node.userObject
        if (userdata is NodeDescriptor<*>) {
            val data = userdata.element
            if (data is LCRecord) {
                val document = PsiDocumentManager.getInstance(project).getDocument(data.file)
                document ?: return

                val startOffset = document.getLineStartOffset(data.record.line) + data.record.col
                list.add(UsageInfo(data.file, startOffset, startOffset + data.record.len))
            }
        }

        myUsagePreviewPanel?.updateLayout(if (list.isEmpty()) null else list)
    }

    override fun getData(dataId: String): Any? {
        if (CommonDataKeys.NAVIGATABLE.`is`(dataId)) {
            val path = tree.selectionPath
            if (path != null && !builder.showPreview) {
                val node = path.lastPathComponent as DefaultMutableTreeNode
                val userObject = node.userObject
                if (userObject is NodeDescriptor<*>) {
                    val element = userObject.element
                    if (element is LCRecord) {
                        return element.getNavigator()
                    }
                }
            }
        }
        return super.getData(dataId)
    }
}

class LuaCheckTreeBuilder(tree: JTree, model: DefaultTreeModel, val project: Project)
    : AbstractTreeBuilder(tree, model, LuaCheckTreeStructure(project), null, false) {
    var isAutoScrollMode: Boolean = false
    var arePackagesShown: Boolean = true
    var showPreview: Boolean = false

    override fun initRootNode() {
        super.initRootNode()
        performUpdate()
    }

    fun clear() {
        val root = rootElement as LCRootNode
        root.clear()
    }

    fun addFile(file: PsiFile): LCPsiFileNode {
        val root = rootElement as LCRootNode
        val fileNode = LCPsiFileNode(project, file)
        root.append(fileNode)
        return fileNode
    }

    fun addLCItem(item: LCRecordData, fileNode: LCPsiFileNode) {
        fileNode.append(LCRecord(project, fileNode.value, item))
    }

    fun performUpdate() {
        queueUpdateFrom(rootNode, true)
    }

    fun collapseAll() {
        var rc = tree.rowCount - 1
        while (rc >= 0) {
            tree.collapseRow(rc)
            rc--
        }
    }

    fun expandAll() {
        ui.expandAll {  }
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

    override fun isAlwaysLeaf(element: Any): Boolean {
        return element is LCRecord
    }
}