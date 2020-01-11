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

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.Processor
import com.tang.intellij.lua.codeInsight.inspection.MatchFunctionSignatureInspection
import com.tang.intellij.lua.comment.psi.LuaDocFunctionTy
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.*

interface IFunSignature {
    val colonCall: Boolean
    val returnTy: ITy
    val params: Array<LuaParamInfo>
    val displayName: String
    val paramSignature: String
    val tyParameters: Array<TyParameter>
    val varargTy: ITy?
    fun substitute(substitutor: ITySubstitutor): IFunSignature
    fun subTypeOf(other: IFunSignature, context: SearchContext, strict: Boolean): Boolean
}

fun IFunSignature.processArgs(callExpr: LuaCallExpr, processor: (index:Int, param: LuaParamInfo) -> Boolean) {
    val expr = callExpr.expr
    val thisTy = if (expr is LuaIndexExpr) {
        expr.guessType(SearchContext.get(expr.project))
    } else null
    processArgs(thisTy, callExpr.isMethodColonCall, processor)
}

fun IFunSignature.processArgs(thisTy: ITy?, colonStyle: Boolean, processor: (index:Int, param: LuaParamInfo) -> Boolean) {
    var index = 0
    var pIndex = 0
    if (colonStyle && !colonCall) {
        pIndex++
    } else if (!colonStyle && colonCall) {
        val pi = LuaParamInfo.createSelf(thisTy)
        if (!processor(index++, pi)) return
    }

    for (i in pIndex until params.size) {
        if (!processor(index++, params[i])) return
    }
}

fun IFunSignature.processArgs(processor: (index:Int, param: LuaParamInfo) -> Boolean) {
    var index = 0
    if (colonCall)
        index++
    for (i in params.indices) {
        if (!processor(index++, params[i])) return
    }
}

fun IFunSignature.processParams(thisTy: ITy?, colonStyle: Boolean, processor: (index:Int, param: LuaParamInfo) -> Boolean) {
    var index = 0
    if (colonCall) {
        val pi = LuaParamInfo.createSelf(thisTy)
        if (!processor(index++, pi)) return
    }

    for (element in params) {
        if (!processor(index++, element)) return
    }
}

fun IFunSignature.getFirstParam(thisTy: ITy?, colonStyle: Boolean): LuaParamInfo? {
    var pi: LuaParamInfo? = null
    processParams(thisTy, colonStyle) { _, paramInfo ->
        pi = paramInfo
        false
    }
    return pi
}

fun IFunSignature.getParamTy(index: Int): ITy {
    val info = params.getOrNull(index)
    return info?.ty ?: Ty.UNKNOWN
}

//eg. print(...)
fun IFunSignature.hasVarargs(): Boolean {
    return this.varargTy != null
}

fun IFunSignature.isGeneric() = tyParameters.isNotEmpty()

abstract class FunSignatureBase(override val colonCall: Boolean,
                                override val params: Array<LuaParamInfo>,
                                override val tyParameters: Array<TyParameter> = emptyArray()
) : IFunSignature {
    override fun equals(other: Any?): Boolean {
        if (other is IFunSignature) {
            if (params.size != other.params.size || returnTy != other.returnTy)
                return false
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

    override val displayName: String by lazy {
        val paramSB = mutableListOf<String>()
        params.forEach {
            paramSB.add(it.name + ":" + it.ty.displayName)
        }
        "fun(${paramSB.joinToString(", ")}):${returnTy.displayName}"
    }

    override val paramSignature: String get() {
        val list = arrayOfNulls<String>(params.size)
        for (i in params.indices) {
            val lpi = params[i]
            list[i] = lpi.name
        }
        return "(" + list.joinToString(", ") + ")"
    }

    override fun substitute(substitutor: ITySubstitutor): IFunSignature {
        val list = params.map { it.substitute(substitutor) }
        return FunSignature(colonCall,
                returnTy.substitute(substitutor),
                varargTy?.substitute(substitutor),
                list.toTypedArray(),
                tyParameters)
    }

    override fun subTypeOf(other: IFunSignature, context: SearchContext, strict: Boolean): Boolean {
        for (i in params.indices) {
            val param = params[i]
            val otherParam = other.params.getOrNull(i) ?: return false
            if (!otherParam.ty.contravariantWith(param.ty, context, strict)) {
                return false
            }
        }

        return other.returnTy.covariantWith(returnTy, context, strict)
    }
}

class FunSignature(colonCall: Boolean,
                   override val returnTy: ITy,
                   override val varargTy: ITy?,
                   params: Array<LuaParamInfo>,
                   tyParameters: Array<TyParameter> = emptyArray()
) : FunSignatureBase(colonCall, params, tyParameters) {

    companion object {
        private fun initParams(func: LuaDocFunctionTy): Array<LuaParamInfo> {
            val list = mutableListOf<LuaParamInfo>()
            func.functionParamList.forEach {
                val p = LuaParamInfo()
                p.name = it.id.text
                p.ty = it.ty?.getType() ?: Ty.UNKNOWN
                list.add(p)
            }
            return list.toTypedArray()
        }

        fun create(colonCall: Boolean, functionTy: LuaDocFunctionTy): IFunSignature {
            val list = mutableListOf<TyParameter>()
            functionTy.genericDefList.forEach { it.name?.let { name -> list.add(TyParameter.getTy(name, it.classNameRef?.text)) } }
            return FunSignature(
                    colonCall,
                    functionTy.returnType,
                    functionTy.varargParam?.type,
                    initParams(functionTy),
                    list.toTypedArray()
            )
        }

        fun serialize(sig: IFunSignature, stream: StubOutputStream) {
            stream.writeBoolean(sig.colonCall)
            Ty.serialize(sig.returnTy, stream)
            stream.writeTyNullable(sig.varargTy)
            stream.writeParamInfoArray(sig.params)
        }

        fun deserialize(stream: StubInputStream): IFunSignature {
            val colonCall = stream.readBoolean()
            val ret = Ty.deserialize(stream)
            val varargTy = stream.readTyNullable()
            val params = stream.readParamInfoArray()
            return FunSignature(colonCall, ret, varargTy, params)
        }
    }
}

interface ITyFunction : ITy {
    val mainSignature: IFunSignature
    val signatures: Array<IFunSignature>
}

val ITyFunction.isColonCall get() = hasFlag(TyFlags.SELF_FUNCTION)

fun ITyFunction.process(processor: Processor<IFunSignature>) {
    // Overloads will always be more (or as) specific as the main signature, so we visit them first.
    for (signature in signatures) {
        if (!processor.process(signature))
            return
    }
    processor.process(mainSignature)
}

fun ITyFunction.findCandidateSignatures(nArgs: Int): Collection<IFunSignature> {
    val candidates = mutableListOf<IFunSignature>()
    process(Processor {
        if (it.params.size >= nArgs) {
            candidates.add(it)
        }
        true
    })
    if (candidates.size == 0) {
        candidates.add(mainSignature)
    }
    return candidates
}

fun ITyFunction.findCandidateSignatures(call: LuaCallExpr): Collection<IFunSignature> {
    val n = call.argList.size
    // 是否是 inst:method() 被用为 inst.method(self) 形式
    val isInstanceMethodUsedAsStaticMethod = isColonCall && call.isMethodDotCall
    if (isInstanceMethodUsedAsStaticMethod)
        return findCandidateSignatures(n - 1)
    val isStaticMethodUsedAsInstanceMethod = !isColonCall && call.isMethodColonCall
    return findCandidateSignatures(if(isStaticMethodUsedAsInstanceMethod) n + 1 else n)
}

class SignatureProblem(val element: PsiElement, val problem: String) {
}

class SignatureMatchResult {
    val problems: MutableMap<IFunSignature, Collection<SignatureProblem>>?
    val signature: IFunSignature?

    constructor(problems: MutableMap<IFunSignature, Collection<SignatureProblem>>) {
        this.problems = problems
        this.signature = null
    }

    constructor(match: IFunSignature?) {
        this.problems = null
        this.signature = match
    }
}

fun ITyFunction.matchSignature(call: LuaCallExpr, searchContext: SearchContext): SignatureMatchResult {
    val concreteParams = call.argList
    val concreteTypes = mutableListOf<MatchFunctionSignatureInspection.ConcreteTypeInfo>()
    concreteParams.forEachIndexed { index, luaExpr ->
        val ty = luaExpr.guessType(searchContext)
        if (ty is TyTuple) {
            if (index == concreteParams.lastIndex) {
                concreteTypes.addAll(ty.list.map { MatchFunctionSignatureInspection.ConcreteTypeInfo(luaExpr, it) })
            } else {
                concreteTypes.add(MatchFunctionSignatureInspection.ConcreteTypeInfo(luaExpr, ty.list.first()))
            }
        } else concreteTypes.add(MatchFunctionSignatureInspection.ConcreteTypeInfo(luaExpr, ty))
    }

    val problems = mutableMapOf<IFunSignature, Collection<SignatureProblem>>()
    val candidates = findCandidateSignatures(call)

    candidates.forEach({
        var nArgs = 0
        val signatureProblems = mutableListOf<SignatureProblem>()

        it.processArgs(call) { i, pi ->
            nArgs = i + 1
            val typeInfo = concreteTypes.getOrNull(i)

            if (typeInfo == null) {
                signatureProblems.add(SignatureProblem(call.lastChild.lastChild, "Missing argument: ${pi.name}: ${pi.ty}"))
                return@processArgs true
            }

            val paramType = pi.ty
            val type = typeInfo.ty
            val assignable: Boolean

            // BEN-TODO:
            /*if (paramType is TyParameter) {
                var resolvedParamType: PsiNamedElement? = null

                LuaDeclarationTree.get(call.containingFile).walkUp(call) { decl ->
                    if (decl.name == paramType.name)
                        resolvedParamType = decl.firstDeclaration.psi
                    resolvedParamType == null
                }

                if (resolvedParamType != null) {
                    assignable = paramType.covariantWith(type, searchContext, false)
                } else {
                    val paramSuperType = paramType.getSuperClass(searchContext)
                    assignable = paramSuperType == null || paramSuperType.covariantWith(type, searchContext, false)
                }
            } else {*/
                assignable = paramType.covariantWith(type, searchContext, false)
            //}

            if (!assignable) {
                signatureProblems.add(SignatureProblem(typeInfo.param, "Type mismatch for argument: ${pi.name}. Required: '${pi.ty}' Found: '$type'"))
            }

            true
        }

        if (nArgs < concreteParams.size && !it.hasVarargs()) {
            for (i in nArgs until concreteParams.size) {
                signatureProblems.add(SignatureProblem(concreteParams[i], "Too many arguments."))
            }
        }


        if (signatureProblems.size == 0) {
            return SignatureMatchResult(it)
        } else {
            problems.put(it, signatureProblems)
        }
    })

    return SignatureMatchResult(problems)
}

abstract class TyFunction : Ty(TyKind.Function), ITyFunction {

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

    override fun covariantWith(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        if (super.covariantWith(other, context, strict)) return true

        var matched = false

        if (other is ITyFunction) {
            process(Processor { sig ->
                other.process(Processor { otherSig ->
                    matched = otherSig.subTypeOf(sig, context, strict)
                    !matched
                })
                !matched
            })
        }
        return matched
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        return substitutor.substitute(this)
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitFun(this)
    }
}

class TyPsiFunction(private val colonCall: Boolean, val psi: LuaFuncBodyOwner, flags: Int = 0) : TyFunction() {
    init {
        this.flags = flags
        if (colonCall) {
            this.flags = this.flags or TyFlags.SELF_FUNCTION
        }
    }

    override val mainSignature: IFunSignature by lazy {

        object : FunSignatureBase(colonCall, psi.params, psi.tyParams) {
            override val returnTy: ITy by lazy {
                var returnTy = psi.guessReturnType(SearchContext.get(psi.project))
                /**
                 * todo optimize this bug solution
                 * local function test()
                 *      return test
                 * end
                 * -- will crash after type `test`
                 */
                if (returnTy is TyPsiFunction && returnTy.psi == psi) {
                    returnTy = UNKNOWN
                }

                returnTy
            }

            override val varargTy: ITy?
                get() = psi.varargType
        }
    }

    override val signatures: Array<IFunSignature> by lazy {
        psi.overloads
    }
}

class TyDocPsiFunction(func: LuaDocFunctionTy) : TyFunction() {
    private val main = FunSignature.create(false, func)
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

object TyFunctionSerializer : TySerializer<ITyFunction>() {
    override fun deserializeTy(flags: Int, stream: StubInputStream): ITyFunction {
        val mainSig = FunSignature.deserialize(stream)
        val arr = stream.readSignatures()
        return TySerializedFunction(mainSig, arr, flags)
    }

    override fun serializeTy(ty: ITyFunction, stream: StubOutputStream) {
        FunSignature.serialize(ty.mainSignature, stream)
        stream.writeSignatures(ty.signatures)
    }
}
