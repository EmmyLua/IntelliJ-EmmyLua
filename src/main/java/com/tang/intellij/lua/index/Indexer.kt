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

import com.tang.intellij.lua.psi.LuaClassMethodDef
import com.tang.intellij.lua.psi.LuaTypeGuessable
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.TyUnknown

interface Indexer {
    val priority get() = 0
    val state: IndexState
    val invalid:Boolean
    fun tryIndex(indexSink: IndexSink, context: SearchContext)

    val done get() = state != IndexState.PENDING
}

enum class IndexState {
    PENDING, OK, FAIL, INVALID
}

class GuessableIndexer(val psi: LuaTypeGuessable, manager: TypeSolverManager) : Indexer {
    private val typeSolver = manager.getSolver(psi)

    private var _state = IndexState.PENDING
    override val state: IndexState get() = _state
    override val priority: Int get() = typeSolver.priority
    override val invalid: Boolean get() = typeSolver.invalid

    override fun tryIndex(indexSink: IndexSink, context: SearchContext) {
        if (typeSolver.invalid) {
            _state = IndexState.INVALID
        }
        else if (typeSolver.solved) {
            typeSolver.index(indexSink)
            _state = IndexState.OK
        }
        else if (typeSolver.dependenceSolved) {
            val ty = psi.guessType(context)
            if (ty !is TyUnknown) {
                typeSolver.solve(ty)
                typeSolver.index(indexSink)
                _state = IndexState.OK
            }
        }
    }
}

class ClassMethodIndexer(private val psi: LuaClassMethodDef, manager: TypeSolverManager) : Indexer {
    private val parentExpr = psi.classMethodName.expr
    private val parentSolver = manager.getSolver(parentExpr)
    private val name = psi.name

    private var _state = IndexState.PENDING
    override val state: IndexState get() = _state

    override val priority: Int get() = parentSolver.priority - 1

    override val invalid: Boolean get() = !psi.isValid || parentSolver.invalid

    override fun tryIndex(indexSink: IndexSink, context: SearchContext) {
        if (name == null) {
            _state = IndexState.FAIL
            return
        }

        val ty = parentExpr.guessType(context)
        if (ty !is TyUnknown) {
            ty.eachTopClass {
                indexSink.indexClassMember(it.className, name, psi)
                true
            }
            _state = IndexState.OK
        }
    }

}

