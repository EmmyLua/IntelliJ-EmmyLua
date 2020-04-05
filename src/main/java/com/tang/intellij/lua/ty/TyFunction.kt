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

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
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
    val tyParameters: Array<TyParameter>?
    val varargTy: ITy?
    fun substitute(substitutor: ITySubstitutor): IFunSignature
    fun contravariantOf(other: IFunSignature, context: SearchContext, flags: Int): Boolean
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

fun IFunSignature.isGeneric() = tyParameters?.isNotEmpty() == true

abstract class FunSignatureBase(override val colonCall: Boolean,
                                override val params: Array<LuaParamInfo>,
                                override val tyParameters: Array<TyParameter>? = null
) : IFunSignature {
    override fun equals(other: Any?): Boolean {
        if (other is IFunSignature) {
            return colonCall == other.colonCall
                    && params.contentEquals(other.params)
                    && tyParameters?.let { other.tyParameters?.contentEquals(it) ?: false } ?: (other.tyParameters == null)
        }
        return false
    }

    override fun hashCode(): Int {
        var code = if (colonCall) 1 else 0
        params.forEach {
            code = code * 31 + it.hashCode()
        }
        tyParameters?.forEach {
            code = code * 31 + it.hashCode()
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

    override fun contravariantOf(other: IFunSignature, context: SearchContext, flags: Int): Boolean {
        for (i in other.params.indices) {
            val param = params.getOrNull(i) ?: return false
            val otherParam = other.params[i]
            if (!otherParam.ty.contravariantOf(param.ty, context, flags)) {
                return false
            }
        }

        return returnTy.contravariantOf(other.returnTy, context, flags)
    }
}

class FunSignature(colonCall: Boolean,
                   override val returnTy: ITy,
                   override val varargTy: ITy?,
                   params: Array<LuaParamInfo>,
                   tyParameters: Array<TyParameter>? = null
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
            return FunSignature(
                    colonCall,
                    functionTy.returnType,
                    functionTy.varargParam?.type,
                    initParams(functionTy),
                    functionTy.genericDefList.map { TyParameter(it) }.toTypedArray()
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
        if (it.params.size >= nArgs  || it.varargTy != null) {
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

class SignatureMatchResult(val signature: IFunSignature, val substitutedSignature: IFunSignature)

fun ITyFunction.matchSignature(call: LuaCallExpr, context: SearchContext, problemsHolder: ProblemsHolder? = null): SignatureMatchResult? {
    val args = call.argList
    val concreteArgTypes = mutableListOf<MatchFunctionSignatureInspection.ConcreteTypeInfo>()
    args.forEachIndexed { index, luaExpr ->
        val ty = if (index == args.lastIndex)
            context.withMultipleResults { luaExpr.guessType(context) }
        else
            context.withIndex(0) { luaExpr.guessType(context) }

        if (!context.supportsMultipleResults && ty is TyMultipleResults) {
            if (index == args.lastIndex) {
                concreteArgTypes.addAll(ty.list.map { MatchFunctionSignatureInspection.ConcreteTypeInfo(luaExpr, it) })
            } else {
                concreteArgTypes.add(MatchFunctionSignatureInspection.ConcreteTypeInfo(luaExpr, ty.list.first()))
            }
        } else concreteArgTypes.add(MatchFunctionSignatureInspection.ConcreteTypeInfo(luaExpr, ty))
    }

    val problems = if (problemsHolder != null) mutableMapOf<IFunSignature, Collection<Problem>>() else null
    val candidates = findCandidateSignatures(call)

    candidates.forEach {
        var nParams = 0
        var candidateFailed = false
        val signatureProblems = if (problems != null) mutableListOf<Problem>() else null

        val substitutor = call.createSubstitutor(it, context)
        val signature = it.substitute(substitutor)

        signature.processArgs(call) { i, pi ->
            nParams = i + 1
            val typeInfo = concreteArgTypes.getOrNull(i)

            if (typeInfo == null) {
                var problemElement = call.lastChild.lastChild

                // Some PSI elements injected by IntelliJ (e.g. PsiErrorElementImpl) can be empty and thus cannot be targeted for our own errors.
                while (problemElement != null && problemElement.textLength == 0) {
                    problemElement = problemElement.prevSibling
                }

                problemElement = problemElement ?: call.lastChild

                candidateFailed = true
                signatureProblems?.add(Problem(null, problemElement, "Missing argument: ${pi.name}: ${pi.ty}"))

                return@processArgs true
            }

            val paramType = pi.ty
            val argType = typeInfo.ty
            val argExpr = args.getOrNull(i) ?: args.last()
            val varianceFlags = if (argExpr is LuaTableExpr) TyVarianceFlags.WIDEN_TABLES else 0

            if (problemsHolder != null) {
                val contravariant = ProblemUtil.contravariantOf(paramType, argType, context, varianceFlags, null, argExpr) { _, element, message, highlightProblem ->
                    signatureProblems?.add(Problem(null, element, message, highlightProblem))
                }

                if (!contravariant) {
                    candidateFailed = true
                }
            } else if (!paramType.contravariantOf(argType, context, varianceFlags)) {
                candidateFailed = true
            }

            true
        }

        if (nParams < concreteArgTypes.size) {
            val varargTy = signature.varargTy

            if (varargTy != null) {
                for (i in nParams until args.size) {
                    val argType = concreteArgTypes.get(i).ty
                    val argExpr = args.get(i)
                    val varianceFlags = if (argExpr is LuaTableExpr) TyVarianceFlags.WIDEN_TABLES else 0

                    if (problemsHolder != null) {
                        val contravariant = ProblemUtil.contravariantOf(varargTy, argType, context, varianceFlags, null, argExpr) { _, element, message, highlightProblem ->
                            signatureProblems?.add(Problem(null, element, message, highlightProblem))
                        }

                        if (!contravariant) {
                            candidateFailed = true
                        }
                    } else if (!varargTy.contravariantOf(argType, context, varianceFlags)) {
                        candidateFailed = true
                    }
                }
            } else {
                if (nParams < args.size) {
                    for (i in nParams until args.size) {
                        candidateFailed = true
                        signatureProblems?.add(Problem(null, args[i], "Too many arguments."))
                    }
                } else {
                    // Last argument is TyMultipleResults, just a weak warning.
                    val excess = nParams - args.size
                    val message = if (excess == 1) "1 result is an excess argument." else "${excess} results are excess arguments."
                    signatureProblems?.add(Problem(null, args.last(), message, ProblemHighlightType.WEAK_WARNING))
                }
            }
        }

        if (!candidateFailed) {
            if (problemsHolder != null) {
                signatureProblems?.forEach {
                    problemsHolder.registerProblem(it.sourceElement, it.message, it.highlightType)
                }
            }

            return SignatureMatchResult(it, signature)
        }

        if (signatureProblems != null) {
            problems?.put(it, signatureProblems)
        }
    }

    if (problemsHolder != null) {
        val multipleCandidates = candidates.size > 1

        problems?.forEach { signature, signatureProblems ->
            signatureProblems.forEach {
                val problem = if (multipleCandidates) "${it.message}. In: ${signature.displayName}\n" else it.message
                problemsHolder.registerProblem(it.sourceElement, problem, it.highlightType)
            }
        }
    }

    return null
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

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        if (super.contravariantOf(other, context, flags)) return true

        var matched = false

        if (other is ITyFunction) {
            process(Processor { sig ->
                other.process(Processor { otherSig ->
                    matched = sig.contravariantOf(otherSig, context, flags)
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
