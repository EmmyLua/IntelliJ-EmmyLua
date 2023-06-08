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
import kotlin.math.max

enum class SolveState {
    Waiting,
    Solved,
    NoSolution
}

class TypeSolver(val sig: ISolverSignature, val dependence: TypeSolver?, val file: FileIndexStore) {
    private var _priority = if (sig.lazyCode.isAssign) 100 else 0
    private var _result: ITy = Ty.UNKNOWN
    private var _state = SolveState.Waiting

    val priority get(): Int = _priority

    val result get() = _result

    val solved get() = _state != SolveState.Waiting

    val state get() = _state

    val invalid get() = file.invalid

    val dependenceSolved: Boolean get() {
        return dependence?.solved ?: true
    }

    init {
        when (sig) {
            is SolvedSolverSignature -> {
                _state = SolveState.Solved
                _result = sig.ty
            }

            is NullSolverSignature -> {
                _state = SolveState.NoSolution
                _result = Ty.UNKNOWN
            }

            else -> dependence?.request(this)
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
        _result = ty
    }

    private fun request(dependency: TypeSolver): ITy? {
        request()
        _priority = max(dependency.priority + 1, _priority)
        return null
    }

    fun request(): ITy? {
        if (solved) return _result

        _priority++
        dependence?.request(this)
        return null
    }
}
