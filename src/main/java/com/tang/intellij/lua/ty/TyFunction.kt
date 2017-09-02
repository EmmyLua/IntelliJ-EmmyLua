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

package com.tang.intellij.lua.ty

import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.tang.intellij.lua.comment.psi.LuaDocFunctionTy
import com.tang.intellij.lua.comment.psi.LuaDocSignatureDef
import com.tang.intellij.lua.comment.psi.resolveDocTypeSet
import com.tang.intellij.lua.psi.LuaClassMethodDef
import com.tang.intellij.lua.psi.LuaCommentOwner
import com.tang.intellij.lua.psi.LuaFuncBodyOwner
import com.tang.intellij.lua.psi.LuaParamInfo
import com.tang.intellij.lua.search.SearchContext

interface IFunSignature {
    val returnTy: ITy
    val params: Array<LuaParamInfo>
    val displayName: String
}

fun IFunSignature.getParamTy(index: Int): ITy {
    val info = params.getOrNull(index)
    return info?.ty ?: Ty.UNKNOWN
}

class FunSignature(override val returnTy: ITy, override val params: Array<LuaParamInfo>) : IFunSignature {
    override fun equals(other: Any?): Boolean {
        if (other is IFunSignature) {
            return params.indices.none { params[it] != other.params.getOrNull(it) }
        }
        return false
    }

    override fun hashCode(): Int {
        var code = returnTy.hashCode()
        params.forEach {
            code += it.ty.hashCode() * 31
        }
        return code
    }

    companion object {
        private fun initParams(func: LuaDocFunctionTy): Array<LuaParamInfo> {
            val list = mutableListOf<LuaParamInfo>()
            func.functionParamList.forEach {
                val p = LuaParamInfo()
                p.name = it.id.text
                p.ty = resolveDocTypeSet(it.typeSet)
                list.add(p)
            }
            return list.toTypedArray()
        }

        fun create(functionTy: LuaDocFunctionTy): IFunSignature {
            return FunSignature(functionTy.returnType, initParams(functionTy))
        }

        fun serialize(sig: IFunSignature, stream: StubOutputStream) {
            Ty.serialize(sig.returnTy, stream)
            stream.writeByte(sig.params.size)
            for (param in sig.params) {
                LuaParamInfo.serialize(param, stream)
            }
        }

        fun deserialize(stream: StubInputStream): IFunSignature {
            val ret = Ty.deserialize(stream)
            val paramSize = stream.readByte()
            val params = mutableListOf<LuaParamInfo>()
            for (j in 0 until paramSize) {
                params.add(LuaParamInfo.deserialize(stream))
            }
            return FunSignature(ret, params.toTypedArray())
        }
    }

    override val displayName: String by lazy {
        val paramSB = mutableListOf<String>()
        params.forEach {
            paramSB.add(it.name + ":" + it.ty.displayName)
        }
        "fun(${paramSB.joinToString(", ")}):${returnTy.displayName}"
    }
}

interface ITyFunction : ITy {
    val mainSignature: IFunSignature
    val signatures: Array<IFunSignature>
}

val ITyFunction.isSelfCall get() = hasFlag(TyFlags.SELF_FUNCTION)

fun ITyFunction.process(processor: Processor<IFunSignature>) {
    if (processor.process(mainSignature)) {
        for (signature in signatures) {
            if (!processor.process(signature))
                break
        }
    }
}

abstract class TyFunction : Ty(TyKind.Function), ITyFunction {
    override val displayName: String
        get() {
            return mainSignature.displayName
        }

    override fun equals(other: Any?): Boolean {
        if (other is ITyFunction) {
            if (mainSignature != other.mainSignature)
                return false
           return signatures.indices.none { signatures[it] != other.signatures.getOrNull(it) }
        }
        return false
    }

    override fun hashCode(): Int {
        var code = mainSignature.hashCode()
        signatures.forEach {
            code += it.hashCode() * 31
        }
        return code
    }
}

class TyPsiFunction(val psi: LuaFuncBodyOwner, searchContext: SearchContext, flags: Int = 0) : TyFunction() {
    init {
        this.flags = flags
        if (psi is LuaClassMethodDef && !psi.isStatic) {
            this.flags = this.flags and TyFlags.SELF_FUNCTION
        }
    }

    override val mainSignature: IFunSignature by lazy {
        FunSignature(psi.guessReturnTypeSet(searchContext), psi.params)
    }

    override val signatures: Array<IFunSignature> by lazy {
        val list = mutableListOf<IFunSignature>()
        if (psi is LuaCommentOwner) {
            val comment = psi.comment
            if (comment != null) {
                val children = PsiTreeUtil.findChildrenOfAnyType(comment, LuaDocSignatureDef::class.java)
                children.forEach {
                    val fty = it.functionTy
                    if (fty != null)
                        list.add(FunSignature.create(fty))
                }
            }
        }
        list.toTypedArray()
    }
}

class TyDocPsiFunction(func: LuaDocFunctionTy) : TyFunction() {
    private val main = FunSignature.create(func)
    override val mainSignature: IFunSignature = main
    override val signatures: Array<IFunSignature> = emptyArray()
}

class TySerializedFunction(override val mainSignature: IFunSignature,
                           override val signatures: Array<IFunSignature>,
                           flags: Int = 0) : TyFunction() {
    init {
        this.flags = flags
    }
}