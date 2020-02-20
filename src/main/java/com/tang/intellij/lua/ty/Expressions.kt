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

import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.psi.impl.LuaDocTagTypeImpl
import com.tang.intellij.lua.ext.recursionGuard
import com.tang.intellij.lua.lang.type.LuaNumber
import com.tang.intellij.lua.lang.type.LuaString
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaNameExprMixin
import com.tang.intellij.lua.psi.search.LuaShortNamesManager
import com.tang.intellij.lua.search.GuardType
import com.tang.intellij.lua.search.SearchContext

fun inferExpr(expr: LuaExpr?, context: SearchContext): ITy {
    if (expr == null)
        return Ty.UNKNOWN

    if (expr.comment != null) {
        val types = PsiTreeUtil.getChildrenOfTypeAsList(expr.comment, LuaDocTagTypeImpl::class.java)

        if (types.size == 1) {
            val castType = types.get(0).ty?.getType()

            if (castType != null) {
                return castType
            }
        }
    }

    if (expr is LuaIndexExpr || expr is LuaNameExpr) {
        val tree = LuaDeclarationTree.get(expr.containingFile)
        val declaration = tree.find(expr)?.firstDeclaration?.psi
        if (declaration != expr && declaration is LuaTypeGuessable) {
            return declaration.guessType(context)
        }
    }
    return inferExprInner(expr, context)
}

private fun inferExprInner(expr: LuaPsiElement, context: SearchContext): ITy {
    return when (expr) {
        is LuaUnaryExpr -> expr.infer(context)
        is LuaBinaryExpr -> expr.infer(context)
        is LuaCallExpr -> expr.infer(context)
        is LuaClosureExpr -> infer(expr, context)
        is LuaTableExpr -> expr.infer(context)
        is LuaParenExpr -> infer(expr.expr, context)
        is LuaNameExpr -> expr.infer(context)
        is LuaLiteralExpr -> expr.infer()
        is LuaIndexExpr -> expr.infer(context)
        else -> Ty.UNKNOWN
    }
}

private fun LuaUnaryExpr.infer(context: SearchContext): ITy {
    val stub = stub
    val operator = if (stub != null) stub.opType else unaryOp.node.firstChildNode.elementType

    return when (operator) {
        LuaTypes.MINUS -> { // Negative something
            val ty = infer(expr, context)
            return if (ty is TyPrimitiveLiteral) ty.primitiveType else ty
        }
        LuaTypes.GETN -> Ty.NUMBER // Table length is a number
        LuaTypes.NOT -> { // Returns a boolean; inverse of a boolean literal
            return when (infer(expr, context).booleanType) {
                Ty.TRUE -> Ty.FALSE
                Ty.FALSE -> Ty.TRUE
                else -> Ty.BOOLEAN
            }
        }
        else -> Ty.UNKNOWN
    }
}

private fun LuaBinaryExpr.infer(context: SearchContext): ITy {
    val stub = stub
    val operator = if (stub != null) stub.opType else {
        val firstChild = firstChild
        val nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(firstChild)
        nextVisibleLeaf?.node?.elementType
    }
    return operator.let {
        when (it) {
        //..
            LuaTypes.CONCAT -> Ty.STRING
        //<=, ==, <, ~=, >=, >
            LuaTypes.LE, LuaTypes.EQ, LuaTypes.LT, LuaTypes.NE, LuaTypes.GE, LuaTypes.GT -> Ty.BOOLEAN
        //and, or
            LuaTypes.AND, LuaTypes.OR -> guessAndOrType(this, operator, context)
        //&, <<, |, >>, ~, ^,    +, -, *, /, //, %
            LuaTypes.BIT_AND, LuaTypes.BIT_LTLT, LuaTypes.BIT_OR, LuaTypes.BIT_RTRT, LuaTypes.BIT_TILDE, LuaTypes.EXP,
            LuaTypes.PLUS, LuaTypes.MINUS, LuaTypes.MULT, LuaTypes.DIV, LuaTypes.DOUBLE_DIV, LuaTypes.MOD -> guessBinaryOpType(this, context)
            else -> Ty.UNKNOWN
        }
    }
}

private fun guessAndOrType(binaryExpr: LuaBinaryExpr, operator: IElementType?, context:SearchContext): ITy {
    val lhs = binaryExpr.left
    val rhs = binaryExpr.right

    val lty = infer(lhs, context)

    //and
    if (operator == LuaTypes.AND) {
        return when (lty.booleanType) {
            Ty.TRUE -> infer(rhs, context)
            Ty.FALSE -> lty
            else -> {
                val u = TyUnion()
                TyUnion.each(lty) {
                    if (it == Ty.BOOLEAN) {
                        u.append(Ty.FALSE)
                    } else if (it.booleanType != Ty.TRUE) {
                        u.append(it)
                    }
                }
                u.append(infer(rhs, context))
            }
        }
    }

    //or
    return when (lty.booleanType) {
        Ty.TRUE -> lty
        Ty.FALSE -> infer(rhs, context)
        else -> {
            val u = TyUnion()
            TyUnion.each(lty) {
                if (it == Ty.BOOLEAN) {
                    u.append(Ty.TRUE)
                } else if (it.booleanType != Ty.FALSE) {
                    u.append(it)
                }
            }
            u.append(infer(rhs, context))
        }
    }
}

private fun guessBinaryOpType(binaryExpr : LuaBinaryExpr, context:SearchContext): ITy {
    val type = infer(binaryExpr.left, context)
    return if (type is TyPrimitiveLiteral) type.primitiveType else type
}

fun LuaCallExpr.createSubstitutor(sig: IFunSignature, context: SearchContext): ITySubstitutor {
    val selfSubstitutor = TySelfSubstitutor(context, this)

    if (sig.isGeneric()) {
        val list = mutableListOf<ITy>()
        // self type
        if (this.isMethodColonCall) {
            this.prefixExpr?.let { prefix ->
                list.add(prefix.guessType(context))
            }
        }
        this.argList.map { list.add(it.guessType(context)) }

        val genericAnalyzer = GenericAnalyzer(sig.tyParameters, context)

        var processedIndex = -1
        sig.processArgs { index, param ->
            val arg = list.getOrNull(index)
            if (arg != null) {
                genericAnalyzer.analyze(arg, param.ty)
            }
            processedIndex = index
            true
        }
        // vararg
        val varargTy = sig.varargTy
        if (varargTy != null && processedIndex < list.lastIndex) {
            val argTy = list[processedIndex + 1]
            genericAnalyzer.analyze(argTy, varargTy)
        }

        val map = genericAnalyzer.map.toMutableMap()
        sig.tyParameters?.forEach {
            val superCls = it.superClass
            if (superCls != null && Ty.isInvalid(map[it.name])) map[it.name] = superCls
        }

        val parameterSubstitutor = TyParameterSubstitutor(map)

        return object : TySubstitutor() {
            override fun substitute(clazz: ITyClass): ITy {
                return clazz.substitute(selfSubstitutor).substitute(parameterSubstitutor)
            }
        }
    }

    return selfSubstitutor
}

private fun LuaCallExpr.getReturnTy(sig: IFunSignature, context: SearchContext): ITy {
    val substitutor = createSubstitutor(sig, context)
    val returnTy = sig.returnTy.substitute(substitutor)
    return if (returnTy is TyTuple) {
        if (context.guessTuple())
            returnTy
        else
            returnTy.list.getOrNull(context.index) ?: Ty.NIL
    } else {
        if (context.guessTuple() || context.index == 0)
            returnTy
        else
            Ty.NIL
    }
}

private fun LuaCallExpr.infer(context: SearchContext): ITy {
    val luaCallExpr = this
    // xxx()
    val expr = luaCallExpr.expr
    // 从 require 'xxx' 中获取返回类型
    if (expr is LuaNameExpr && LuaSettings.isRequireLikeFunctionName(expr.name)) {
        var filePath: String? = null
        val string = luaCallExpr.firstStringArg
        if (string is LuaLiteralExpr) {
            filePath = string.stringValue
        }
        var file: LuaPsiFile? = null
        if (filePath != null)
            file = resolveRequireFile(filePath, luaCallExpr.project)
        if (file != null)
            return file.guessType(context)

        return Ty.UNKNOWN
    }

    var ret: ITy = Ty.VOID
    val ty = infer(expr, context)//expr.guessType(context)
    TyUnion.each(ty) {
        when (it) {
            is ITyFunction -> {
                val match = it.matchSignature(this, context)

                if (match.substitutedSignature != null) {
                    ret = ret.union(getReturnTy(match.substitutedSignature, context))
                }
            }
            //constructor : Class table __call
            is ITyClass -> ret = ret.union(it)
        }
    }

    // xxx.new()
    if (expr is LuaIndexExpr) {
        val fnName = expr.name
        if (fnName != null && LuaSettings.isConstructorName(fnName)) {
            ret = ret.union(expr.guessParentType(context))
        }
    }

    return if (Ty.isInvalid(ret)) Ty.UNKNOWN else ret
}

private fun LuaNameExpr.infer(context: SearchContext): ITy {
    if (this.id.text == Constants.WORD_SELF) {
        val contextClass = LuaPsiTreeUtil.findContextClass(this) as? ITyClass
        return if (contextClass != null) TyClass.createSelfType(contextClass) else Ty.UNKNOWN
    }

    val set = recursionGuard(this, Computable {
        var type:ITy = Ty.VOID

        context.withRecursionGuard(this, GuardType.GlobalName) {
            val multiResolve = multiResolve(this, context)
            var maxTimes = 10
            for (element in multiResolve) {
                val set = getType(context, element)
                type = type.union(set)
                if (--maxTimes == 0)
                    break
            }
            type
        }

        /**
         * fixme : optimize it.
         * function xx:method()
         *     self.name = '123'
         * end
         *
         * https://github.com/EmmyLua/IntelliJ-EmmyLua/issues/93
         * the type of 'self' should be same of 'xx'
         */
        if (Ty.isInvalid(type)) {
            if (name == Constants.WORD_SELF) {
                val methodDef = PsiTreeUtil.getStubOrPsiParentOfType(this, LuaClassMethodDef::class.java)
                if (methodDef != null && !methodDef.isStatic) {
                    val methodName = methodDef.classMethodName
                    val expr = methodName.expr
                    type = expr.guessType(context)
                }
            }
        }

        if (Ty.isInvalid(type)) {
            type = getType(context, this)
        }

        type
    })
    return set ?: Ty.UNKNOWN
}

private fun getType(context: SearchContext, def: PsiElement): ITy {
    when (def) {
        is LuaNameExpr -> {
            //todo stub.module -> ty
            val stub = def.stub
            stub?.module?.let {
                val memberType = createSerializedClass(it).findMemberType(def.name, context)
                if (memberType != null && !Ty.isInvalid(memberType))
                    return memberType
            }

            var type: ITy = def.docTy ?: Ty.VOID
            //guess from value expr
            if (Ty.isInvalid(type) /*&& !context.forStub*/) {
                val stat = def.assignStat
                if (stat != null) {
                    val exprList = stat.valueExprList
                    if (exprList != null) {
                        type = context.withIndex(stat.getIndexFor(def)) {
                            exprList.guessTypeAt(context)
                        }
                    }
                }
            }

            //Global
            if (isGlobal(def) && type !is ITyPrimitive) {
                //use globalClassTy to store class members, that's very important
                type = type.union(TyClass.createGlobalType(def, context.forStub))
            }
            return if (Ty.isInvalid(type)) Ty.UNKNOWN else type
        }
        is LuaTypeGuessable -> return def.guessType(context)
        else -> return Ty.UNKNOWN
    }
}

private fun isGlobal(nameExpr: LuaNameExpr):Boolean {
    val minx = nameExpr as LuaNameExprMixin
    val gs = minx.greenStub
    return gs?.isGlobal ?: (resolveLocal(nameExpr, null) == null)
}

private fun LuaLiteralExpr.infer(): ITy {
    return when (this.kind) {
        LuaLiteralKind.Bool -> TyPrimitiveLiteral.getTy(TyPrimitiveKind.Boolean, firstChild.text)
        LuaLiteralKind.Nil -> Ty.NIL
        LuaLiteralKind.Number -> {
            val n = LuaNumber.getValue(firstChild.text)
            if (n != null) TyPrimitiveLiteral.getTy(TyPrimitiveKind.Number, n.toString()) else Ty.UNKNOWN
        }
        LuaLiteralKind.String -> TyPrimitiveLiteral.getTy(TyPrimitiveKind.String, LuaString.getContent(firstChild.text).value)
        LuaLiteralKind.Varargs -> {
            val o = PsiTreeUtil.getParentOfType(this, LuaFuncBodyOwner::class.java)
            o?.varargType ?: Ty.UNKNOWN
        }
        //LuaLiteralKind.Nil -> Ty.NIL
        else -> Ty.UNKNOWN
    }
}

private fun LuaIndexExpr.infer(context: SearchContext): ITy {
    val retTy = recursionGuard(this, Computable {
        val indexExpr = this
        var parentTy: ITy? = null
        // xxx[yyy] as an array element?
        if (indexExpr.brack) {
            val tySet = indexExpr.guessParentType(context)
            var ty: ITy = Ty.VOID

            // Type[]
            TyUnion.each(tySet) {
                if (it is ITyArray) ty = ty.union(it.base)
            }
            if (!Ty.isInvalid(ty) && ty !is TyUnknown) return@Computable ty

            // table<number, Type>
            TyUnion.each(tySet) {
                if (it is ITyGeneric) ty = ty.union(it.getParamTy(1))
            }
            if (!Ty.isInvalid(ty) && ty !is TyUnknown) return@Computable ty

            parentTy = tySet
        }

        //from @type annotation
        val docTy = indexExpr.docTy
        if (docTy != null)
            return@Computable docTy

        // xxx.yyy = zzz
        //from value
        var result: ITy = Ty.VOID
        val assignStat = indexExpr.assignStat
        if (assignStat != null) {
            result = context.withIndex(assignStat.getIndexFor(indexExpr)) {
                assignStat.valueExprList?.guessTypeAt(context) ?: Ty.VOID
            }
        }

        //from other class member
        val propName = indexExpr.name
        if (propName != null) {
            val prefixType = parentTy ?: indexExpr.guessParentType(context)
            prefixType.each { ty ->
                result = result.union(guessFieldType(propName, ty, context))
            }
        }

        if (Ty.isInvalid(result)) Ty.VOID else result
    })

    return retTy ?: Ty.VOID
}

private fun guessFieldType(fieldName: String, type: ITy, context: SearchContext): ITy {
    val cls = (if (type is TyGeneric) type.base else type) as? TyClass

    if (cls != null) {
        // _G.var = {}  <==>  var = {}
        if (cls.className == Constants.WORD_G)
            return TyClass.createGlobalType(fieldName)

        val types = mutableListOf<Pair<ITy, ITyClass?>>()

        LuaShortNamesManager.getInstance(context.project).processAllMembers(cls, fieldName, context, Processor {
            val fieldType = it.guessType(context)
            var fieldClass: ITyClass? = null

            // Generic parameters in fields need to be substituted, keeping in mind ITyGeneric may recursively contain generic parameter.
            if (fieldType is TyParameter || fieldType is ITyGeneric) {
                fieldClass = it.guessClassType(context)
            }

            types.add(Pair(fieldType, fieldClass))
            true
        })

        if (types.size == 0) {
            return Ty.NIL // Lua returns nil for access of non-existent fields
        }

        return types.fold(Ty.VOID as ITy, { union, (fieldTy, fieldClass) ->
            var ty = fieldTy

            if (fieldClass != null) {
                var generic = type as? ITyGeneric

                while (generic != null && generic.base != fieldClass) {
                    generic = generic.getSuperClass(context) as? ITyGeneric
                }

                if (generic != null) {
                    val paramMap = mutableMapOf<String, ITy>()

                    fieldClass.getParams(context)?.forEachIndexed { index, classParam ->
                        if (index < generic.params.size) {
                            paramMap[classParam.varName] = generic.params[index]
                        }
                    }

                    ty = ty.substitute(TyParameterSubstitutor(paramMap))
                }
            }

            return union.union(ty)
        })
    }

    return Ty.VOID
}

private fun LuaTableExpr.infer(context: SearchContext): ITy {
    val list = this.tableFieldList

    if (list.size == 0) {
        return TyTable(this)
    }

    if (list.size == 1) {
        val field = list.first()

        if (field.id == null) {
            val exprList = field.exprList

            if (exprList.size == 1) {
                val valueExpr = exprList[0]

                if (valueExpr is LuaLiteralExpr && valueExpr.kind == LuaLiteralKind.Varargs) {
                    val func = PsiTreeUtil.getStubOrPsiParentOfType(this, LuaFuncBodyOwner::class.java)
                    val ty = func?.varargType
                    if (ty != null) {
                        return TyArray(ty)
                    }
                } else {
                    return TyArray(valueExpr.guessType(context))
                }
            }
        }

        return TyTable(this)
    }

    var keyType: ITy = Ty.VOID
    var elementType: ITy = Ty.VOID

    list.forEach {
        val exprList = it.exprList

        if (exprList.size == 2) {
            keyType = keyType.union(exprList[0].guessType(context))
            elementType = elementType.union(exprList[1].guessType(context))
        } else {
            if (it.id != null) {
                keyType = keyType.union(Ty.STRING)
            }
            elementType = elementType.union(exprList[0].guessType(context))
        }
    }

    if (Ty.isInvalid(keyType)) {
        return if (Ty.isInvalid(elementType)) Ty.UNKNOWN else TyArray(elementType)
    }

    return TyTable(this)
}
