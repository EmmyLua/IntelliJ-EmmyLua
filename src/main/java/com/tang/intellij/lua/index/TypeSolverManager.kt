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

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.util.indexing.IndexId
import com.tang.intellij.lua.ext.fileId
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.ty.infer

interface TypeSolverListener {
    fun onNewCreated(solver: TypeSolver)
}

class TypeSolverManager : IndexSink {

    private val fileStoreMap = mutableMapOf<Int, FileIndexStore>()
    private var listener: TypeSolverListener? = null

    fun setListener(listener: TypeSolverListener) {
        this.listener = listener
    }

    private fun getFile(sig: ISolverSignature): FileIndexStore {
        return fileStoreMap.getOrPut(sig.lazyCode.fileId) { FileIndexStore(sig.lazyCode.fileId) }
    }

    fun cleanFile(fileId: Int) {
        fileStoreMap.remove(fileId)?.clean()
    }

    fun getSolver(psi: LuaTypeGuessable): TypeSolver {
        val sig = psi.createSignature()
        return getSolver(sig)
    }

    private fun getSolver(sig: ISolverSignature): TypeSolver {
        val file = getFile(sig)
        val result = file.getSolver(sig)
        if (result != null) return result

        val parent = sig.parent?.let { getSolver(it) }
        val solver = TypeSolver(sig, parent, file)
        file.setSolver(sig, solver)
        listener?.onNewCreated(solver)
        return solver
    }

    companion object {
        private val key = Key.create<TypeSolverManager>("lua.lazyManager")

        fun getInstance(project: Project): TypeSolverManager {
            var manager = key.get(project)
            if (manager == null) {
                manager = TypeSolverManager()
                project.putUserData(key, manager)
            }
            return manager
        }
    }

    override fun indexClassMember(clazz: String, name: String, psi: LuaClassMember) {
        this.occurrence(StubKeys.CLASS_MEMBER, clazz.hashCode(), psi)
        this.occurrence(StubKeys.CLASS_MEMBER, "$clazz*$name".hashCode(), psi)
    }

    override fun indexShortName(name: String, psi: LuaPsiElement) {
        this.occurrence(StubKeys.SHORT_NAME, name, psi)
    }

    override fun <Psi : PsiElement, K> occurrence(indexKey: IndexId<K, Psi>, key: K, value: Psi) {
        val fileId = value.containingFile.fileId
        when (indexKey) {
            StubKeys.SHORT_NAME -> ShortNameIndex.instance.occurrence(fileId, key, value)
            StubKeys.CLASS_MEMBER -> ClassMemberIndex.instance.occurrence(fileId, key, value)
        }
    }
}

private fun LuaTypeGuessable.createSignature(): ISolverSignature {
    return when (this) {
        is LuaCallExpr -> {
            val expr = this.expr
            /*if (expr is LuaNameExpr && LuaSettings.isRequireLikeFunctionName(expr.name)) {
                TODO("implements request")
            }*/
            val parent = expr.createSignature()
            SimpleSolverSignature(this, parent, LazyCode.make(this))
        }
        is LuaIndexExpr -> {
            val parent = this.prefixExpr.createSignature()

            // xxx[yyy]
            if (this.brack) {
                ArrayIndexSolverSignature(this, parent, LazyCode.make(this))
            }
            // x.x / x["x"]
            else {
                val name = this.name
                if (name == null)
                    NullSolverSignature
                else
                    IndexSolverSignature(name, this, parent, LazyCode.make(this))
            }
        }
        is LuaNameExpr -> {
            val local = resolveLocal(this)
            if (local == null) {
                SimpleSolverSignature(this, null, LazyCode.make(this))
            } else {
                SimpleSolverSignature(this, null, LazyCode.make(local))
            }
        }
        is LuaTableExpr -> {
            SolvedSolverSignature(this.infer(), this, null, LazyCode.make(this))
        }
        is LuaLiteralExpr -> {
            SolvedSolverSignature(this.infer(), this, null, LazyCode.make(this))
        }
        is LuaUnaryExpr -> SimpleSolverSignature(this, null, LazyCode.make(this))
        else -> SimpleSolverSignature(this, null, LazyCode.make(this))
    }
}