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

import com.intellij.openapi.vfs.newvfs.ManagingFS

class FileIndexStore(private val fileId: Int) {

    private var _invalid = false

    val invalid get() = _invalid

    private val solverMap = mutableMapOf<LazyCode, TypeSolver>()

    val file get() = ManagingFS.getInstance().findFileById(fileId)

    fun getSolver(sig: ISolverSignature): TypeSolver? {
        assert(sig.lazyCode.fileId == fileId)
        return solverMap[sig.lazyCode]
    }

    fun setSolver(sig: ISolverSignature, ty: TypeSolver) {
        solverMap[sig.lazyCode] = ty
    }

    fun clean() {
        _invalid = true
        StubKeys.removeStubs(fileId)
        solverMap.clear()
    }
}