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

import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyUnknown

enum class SolverType {
    Reference,
    Definition
}

enum class SolveState {
    Waiting,
    Solved,
    NoSolution
}

class TypeSolver(val sig: ISolverSignature, val dependence: TypeSolver?, val file: FileIndexStore) {
    private var _priority = 0
    private var _trueTy: ITy = Ty.UNKNOWN
    private var _state = SolveState.Waiting
    private var _type = SolverType.Definition

    val priority get(): Int = _priority

    val trueTy get() = _trueTy

    val solved get() = _state != SolveState.Waiting

    val state get() = _state

    val type get() = _type

    val invalid get() = file.invalid

    val dependenceSolved: Boolean get() {
        return dependence?.solved ?: true
    }

    init {
        when (sig) {
            is SolvedSolverSignature -> {
                _state = SolveState.Solved
                _trueTy = sig.ty
            }

            is NullSolverSignature -> {
                _state = SolveState.NoSolution
                _trueTy = Ty.UNKNOWN
            }

            else -> dependence?.request()
        }
    }

    fun index(indexSink: IndexSink) {
        assert(solved && dependenceSolved)
        sig.index(this, indexSink)
    }

    fun solve(ty: ITy) {
        if (ty is TyUnknown)
            return

        assert(dependenceSolved)
        _state = SolveState.Solved
        _trueTy = ty
    }

    fun markAsNoSolution() {
        _state = SolveState.NoSolution
        _trueTy = Ty.UNKNOWN
    }

    fun request(): ITy? {
        if (solved) return _trueTy

        dependence?.request()
        _priority++
        return null
    }
}
