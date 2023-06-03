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
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.indexing.FileBasedIndex
import com.tang.intellij.lua.ext.fileId
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITy
import java.util.concurrent.atomic.AtomicBoolean

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

    private val solverManager = TypeSolverManager.getInstance(project)
    private val changedFiles = mutableSetOf<LuaPsiFile>()
    private var scanType = ScanType.All
    private val task = IndexTask(project)
    private val taskQueue = TaskQueue(project)

    init {
        solverManager.setListener(task)
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

    fun remove(element: PsiElement) {
        val file = element.containingFile
        solverManager.cleanFile(file.fileId)
    }

    override fun afterWriteActionFinished(action: Any) {
        runScan()
    }

    fun tryInfer(target: LuaTypeGuessable): ITy? {
        val typeSolver = solverManager.getSolver(target)
        if (typeSolver.solved)
            return typeSolver.result

        if (task.isRunning) {
            typeSolver.request()
        } else {
            task.scan(target)
            task.run(EmptyProgressIndicator())
        }
        return if (typeSolver.solved) typeSolver.result else null
    }

    private fun runScan() {
        when (scanType) {
            ScanType.None -> { }
            ScanType.All -> {
                taskQueue.runReadAction("Lua indexing ...", ::scanAllProject)
            }
            ScanType.Changed -> scanChangedFiles()
        }
        scanType = ScanType.None
    }

    private fun scanChangedFiles() {
        changedFiles.forEach {
            task.scan(it)
        }
        changedFiles.clear()
        task.run(EmptyProgressIndicator())
    }

    private fun scanAllProject(indicator: ProgressIndicator) {
        indicator.pushState()
        indicator.text = "Collecting ..."
        val startAt = System.currentTimeMillis()
        FileBasedIndex.getInstance().iterateIndexableFiles({ vf ->
            if (vf.fileType == LuaFileType.INSTANCE) {
                PsiManager.getInstance(project).findFile(vf).let {
                    if (it is LuaPsiFile) task.scan(it)
                }
            }
            true
        }, project, null)
        indicator.popState()

        indicator.pushState()
        val dt = System.currentTimeMillis() - startAt
        logger.info("Scan all lua files in project, took $dt ms.")
        task.run(indicator)
        indicator.popState()
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

    private val solverManager = TypeSolverManager.getInstance(project)
    private val indexers = mutableListOf<Indexer>()
    private val additional = mutableListOf<Indexer>()
    private var running = AtomicBoolean(false)

    val isRunning get() = running.get()

    fun scan(psi: LuaTypeGuessable) {
        add(solverManager.getSolver(psi))
    }

    fun scan(file: LuaPsiFile) {
        solverManager.cleanFile(file.fileId)

        file.accept(object: LuaStubRecursiveVisitor() {

            override fun visitExpr(o: LuaExpr) {
                if (o is LuaIndexExpr && o.assignStat != null) {
                    add(solverManager.getSolver(o))
                }
                else if (o is LuaNameExpr && o.assignStat != null) {
                    add(solverManager.getSolver(o))
                }
                super.visitExpr(o)
            }

            override fun visitClassMethodDef(o: LuaClassMethodDef) {
                add(ClassMethodIndexer(o, solverManager))
                super.visitClassMethodDef(o)
            }
        })
    }

    fun run(indicator: ProgressIndicator) {
        if (indexers.isEmpty())
            return

        running.set(true)
        val report = IndexReport()
        try {
            run(indicator, report)
        }
        catch (e: ProcessCanceledException) {
            println(e.message)
        }
        catch (e: Exception) {
            println(e.message)
        }
        indexers.clear()
        report.report()
        running.set(false)
    }

    @Synchronized
    private fun run(indicator: ProgressIndicator, report: IndexReport) {
        var total = indexers.size
        val context = SearchContext.get(project)

        report.total = total
        while (total > 0) {
            indexers.sortByDescending { it.priority }

            for (item in indexers) {
                item.tryIndex(solverManager, context)
            }
            indicator.text = "try index: ${report.total}"

            indexers.removeAll { it.done }

            synchronized(additional) {
                indexers.addAll(additional)
                report.total += additional.size
                additional.clear()
            }

            if (indexers.count() == total)
                break
            total = indexers.count()
        }

        report.noSolution = indexers.size
    }

    private fun add(solver: TypeSolver) {
        if (solver.sig is NullSolverSignature)
            return

        add(GuessableIndexer(solver.sig.psi, solverManager))
        solver.dependence?.let { add(it) }
    }

    private fun add(indexer: Indexer) {
        if (isRunning) {
            synchronized(additional) {
                additional.add(indexer)
            }
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
