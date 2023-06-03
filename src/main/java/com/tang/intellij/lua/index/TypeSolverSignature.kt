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

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.ObjectStubBase
import com.tang.intellij.lua.ext.fileId
import com.tang.intellij.lua.psi.LuaIndexExpr
import com.tang.intellij.lua.psi.LuaNameExpr
import com.tang.intellij.lua.psi.LuaTypeGuessable
import com.tang.intellij.lua.ty.ITy

class LazyCode(
    val fileId: Int,
    val pos: Int,
    val code: Int,
    private val debugString: String? = null) {

    private var isGlobal = false

    override fun toString(): String {
        if (debugString != null) return debugString
        return super.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LazyCode) return false
        if (isGlobal) {
            return other.isGlobal && code == other.code
        }

        if (fileId != other.fileId) return false
        if (pos != other.pos) return false
        if (code != other.code) return false

        return true
    }

    override fun hashCode(): Int {
        if (isGlobal) return code

        var result = fileId
        result = 31 * result + pos
        result = 31 * result + code
        return result
    }

    companion object {
        fun make(psi: LuaIndexExpr): LazyCode {
            val name = psi.name
            val id = name?.hashCode() ?: 0
            return create(psi, id)
        }

        fun make(psi: LuaNameExpr): LazyCode {
            val code = psi.name.hashCode()
            val ret = create(psi, code)
            ret.isGlobal = true
            return ret
        }

        fun make(psi: PsiElement): LazyCode {
            return create(psi, 0)
        }

        private fun create(psi: PsiElement, code: Int): LazyCode {
            var id = 0
            if (psi is StubBasedPsiElementBase<*>) {
                val stub = psi.stub
                if (stub is ObjectStubBase<*>) {
                    id = stub.stubId
                }
            }
            if (id == 0) {
                id = psi.textOffset
            }
            return LazyCode(getFileId(psi), id, code)
        }

        private fun getFileId(psi: PsiElement): Int {
            val file = psi.containingFile.originalFile
            return file.fileId
        }
    }
}

interface ISolverSignature {
    val psi: LuaTypeGuessable
    val parent: ISolverSignature?
    val lazyCode: LazyCode

    fun index(ty: TypeSolver, indexSink: IndexSink) { }
}

/**
 * val = x
 */
class SimpleSolverSignature(
    override val psi: LuaTypeGuessable,
    override val parent: ISolverSignature?,
    override val lazyCode: LazyCode
) : ISolverSignature

/**
 * val = x.x
 */
class IndexSolverSignature(
    private val name: String,
    override val psi: LuaIndexExpr,
    override val parent: ISolverSignature,
    override val lazyCode: LazyCode
) : ISolverSignature {
    override fun index(ty: TypeSolver, indexSink: IndexSink) {
        ty.dependence?.result?.eachTopClass {
            indexSink.indexClassMember(it.className, name, psi)
            true
        }
    }
}

/**
 * val = x[1]
 */
class ArrayIndexSolverSignature(
    override val psi: LuaIndexExpr,
    override val parent: ISolverSignature,
    override val lazyCode: LazyCode
) : ISolverSignature

class SolvedSolverSignature(
    val ty: ITy,
    override val psi: LuaTypeGuessable,
    override val parent: ISolverSignature?,
    override val lazyCode: LazyCode
) : ISolverSignature

object NullSolverSignature : ISolverSignature
{
    override val psi: LuaTypeGuessable
        get() = TODO("Not yet implemented")
    override val parent: ISolverSignature? = null
    override val lazyCode: LazyCode = LazyCode(0,0,0, "null")
}