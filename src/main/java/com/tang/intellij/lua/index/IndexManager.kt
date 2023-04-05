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

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.indexing.FileBasedIndex
import com.tang.intellij.lua.ext.fileId
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITy

class LuaProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        IndexManager.getInstance(project)
    }
}

class IndexManager(private val project: Project) : Disposable, ApplicationListener {

    enum class ScanType {
        None,
        All,
        Changed
    }

    private val lazyManager = TypeSolverManager.getInstance(project)
    private val changedFiles = mutableSetOf<LuaPsiFile>()
    private var scanType = ScanType.All
    private val task = IndexTask(project)

    init {
        lazyManager.setListener(task)
        setupListeners(project)
    }

    private fun setupListeners(project: Project) {
        val treeChangeListener = LuaPsiTreeChangeListener(this)
        PsiManager.getInstance(project).addPsiTreeChangeListener(treeChangeListener, this)
        ApplicationManager.getApplication().addApplicationListener(this, this)
    }

    fun onFileUpdate(file: PsiFile) {
        if (file is LuaPsiFile) {
            changedFiles.add(file)
            scanType = ScanType.Changed
        }
    }

    override fun afterWriteActionFinished(action: Any) {
        runScan()
    }

    fun tryInfer(target: LuaTypeGuessable, searchContext: SearchContext): ITy? {
        val lazyTy = lazyManager.getSolver(target)
        if (lazyTy.solved)
            return lazyTy.trueTy

        if (task.isRunning) {
            lazyTy.request()
        } else {
            task.scan(target)
            task.run()
        }
        return if (lazyTy.solved) lazyTy.trueTy else null
    }

    private fun runScan() {
        when (scanType) {
            ScanType.None -> { }
            ScanType.All -> scanAllProject()
            ScanType.Changed -> scanChangedFiles()
        }
        scanType = ScanType.None
    }

    private fun scanChangedFiles() {
        changedFiles.forEach {
            task.scan(it)
        }
        changedFiles.clear()
        task.run()
    }

    private fun scanAllProject() {
        val startAt = System.currentTimeMillis()
        FileBasedIndex.getInstance().iterateIndexableFiles({ vf ->
            if (vf.fileType == LuaFileType.INSTANCE) {
                PsiManager.getInstance(project).findFile(vf).let {
                    if (it is LuaPsiFile) task.scan(it)
                }
            }
            true
        }, project, null)

        val dt = System.currentTimeMillis() - startAt
        logger.info("Scan all lua files in project, took $dt ms.")
        task.run()
    }

    override fun dispose() {
    }

    companion object {
        private val logger = Logger.getInstance(IndexManager::class.java)

        fun getInstance(project: Project): IndexManager = project.getService(IndexManager::class.java)
    }
}

class IndexReport {
    var total = 0
    var noSolution = 0

    private val startAt = System.currentTimeMillis()

    fun report() {
        val dt = System.currentTimeMillis() - startAt
        val message = "Took $dt ms, total = $total, no solution = $noSolution"
        logger.debug(message)
    }

    companion object {
        private val logger = Logger.getInstance(IndexReport::class.java)
    }
}

class IndexTask(private val project: Project) : TypeSolverListener {

    private val manager = TypeSolverManager.getInstance(project)
    private val indexers = mutableListOf<Indexer>()
    private val additional = mutableListOf<Indexer>()
    private var running = false

    val isRunning get() = running

    fun scan(psi: LuaTypeGuessable) {
        add(manager.getSolver(psi))
    }

    fun scan(file: LuaPsiFile) {
        manager.cleanFile(file.fileId)

        file.accept(object: LuaRecursiveVisitor() {

            override fun visitExpr(o: LuaExpr) {
                if (o is LuaIndexExpr && o.assignStat != null) {
                    add(manager.getSolver(o))
                }
                else if (o is LuaNameExpr && o.assignStat != null) {
                    add(manager.getSolver(o))
                }
                super.visitExpr(o)
            }

            override fun visitClassMethodDef(o: LuaClassMethodDef) {
                add(ClassMethodIndexer(o, manager))
                super.visitClassMethodDef(o)
            }
        })
    }

    fun run() {
        if (indexers.isEmpty())
            return

        running = true

        val report = IndexReport()
        var total = indexers.size
        val context = SearchContext.get(project)

        report.total = total
        while (total > 0) {
            indexers.sortByDescending { it.priority }

            for (item in indexers) {
                item.tryIndex(manager, context)
            }

            indexers.removeAll { it.done }
            indexers.addAll(additional)
            report.total += additional.size
            additional.clear()
            if (indexers.count() == total)
                break
            total = indexers.count()
        }

        report.noSolution = indexers.size
        report.report()
        indexers.clear()
        running = false
    }

    private fun add(solver: TypeSolver) {
        if (solver.sig is NullSolverSignature)
            return

        add(GuessableIndexer(solver.sig.psi, manager))
        solver.dependence?.let { add(it) }
    }

    private fun add(indexer: Indexer) {
        if (isRunning) {
            additional.add(indexer)
        } else {
            indexers.add(indexer)
        }
    }

    override fun onNewCreated(solver: TypeSolver) {
        if (isRunning) {
            add(solver)
        }
    }
}