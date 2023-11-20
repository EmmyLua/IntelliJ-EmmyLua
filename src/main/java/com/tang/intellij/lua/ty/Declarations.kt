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

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.openapi.util.Computable
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocTagField
import com.tang.intellij.lua.comment.psi.LuaDocTagReturn
import com.tang.intellij.lua.ext.recursionGuard
import com.tang.intellij.lua.ext.stubOrPsiParent
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.GuardType
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaFuncBodyOwnerStub

fun infer(element: LuaTypeGuessable?, context: SearchContext): ITy {
    if (element == null)
        return Ty.UNKNOWN
    return SearchContext.infer(element, context)
}

internal fun inferInner(element: LuaTypeGuessable, context: SearchContext): ITy {
    return when (element) {
        is LuaFuncBodyOwner -> element.infer(context)
        is LuaExpr -> inferExpr(element, context)
        is LuaParamNameDef -> element.infer(context)
        is LuaNameDef -> element.infer(context)
        is LuaDocTagField -> element.infer()
        is LuaTableField -> element.infer(context)
        is LuaPsiFile -> inferFile(element, context)
        else -> Ty.UNKNOWN
    }
}

fun inferReturnTy(owner: LuaFuncBodyOwner, searchContext: SearchContext): ITy {
    if (owner is StubBasedPsiElementBase<*>) {
        val stub = owner.stub
        if (stub is LuaFuncBodyOwnerStub<*>) {
            return stub.guessReturnTy(searchContext)
        }
    }

    return inferReturnTyInner(owner, searchContext)
}

private fun inferReturnTyInner(owner: LuaFuncBodyOwner, searchContext: SearchContext): ITy {
    if (owner is LuaCommentOwner) {
        val comment = LuaCommentUtil.findComment(owner)
        if (comment != null) {
            val returnDef = PsiTreeUtil.findChildOfType(comment, LuaDocTagReturn::class.java)
            if (returnDef != null) {
                //return returnDef.resolveTypeAt(searchContext.index)
                return returnDef.type
            }
        }
    }

    //infer from return stat
    return searchContext.withRecursionGuard(owner, GuardType.RecursionCall) {
        var type: ITy = Ty.VOID
        owner.acceptChildren(object : LuaRecursiveVisitor() {
            override fun visitReturnStat(o: LuaReturnStat) {
                val guessReturnType = guessReturnType(o, searchContext.index, searchContext)
                TyUnion.each(guessReturnType) {
                    /**
                     * 注意，不能排除anonymous
                     * local function test()
                     *      local v = xxx()
                     *      v.yyy = zzz
                     *      return v
                     * end
                     *
                     * local r = test()
                     *
                     * type of r is an anonymous ty
                     */
                    type = type.union(it)
                }
            }

            override fun visitExprStat(o: LuaExprStat) {}
            override fun visitLabelStat(o: LuaLabelStat) {}
            override fun visitAssignStat(o: LuaAssignStat) {}
            override fun visitGotoStat(o: LuaGotoStat) {}
            override fun visitClassMethodDef(o: LuaClassMethodDef) {}
            override fun visitFuncDef(o: LuaFuncDef) {}
            override fun visitLocalDef(o: LuaLocalDef) {}
            override fun visitLocalFuncDef(o: LuaLocalFuncDef) {}
        })
        type
    }
}

private fun LuaParamNameDef.infer(context: SearchContext): ITy {
    var type = resolveParamType(this, context)
    //anonymous
    if (Ty.isInvalid(type))
        type = TyClass.createAnonymousType(this)
    return type
}

private fun LuaNameDef.infer(context: SearchContext): ITy {
    var type: ITy = Ty.UNKNOWN
    val parent = this.stubOrPsiParent
    if (parent is LuaTableField) {
        val expr = PsiTreeUtil.findChildOfType(parent, LuaExpr::class.java)
        if (expr != null)
            type = infer(expr, context)
    } else {
        val docTy = this.docTy
        if (docTy != null)
            return docTy

        val localDef = PsiTreeUtil.getStubOrPsiParentOfType(this, LuaLocalDef::class.java)
        if (localDef != null) {
            //计算 expr 返回类型
            if (Ty.isInvalid(type) /*&& !context.forStub*/) {
                val nameList = localDef.nameList
                val exprList = localDef.exprList
                if (nameList != null && exprList != null) {
                    type = context.withIndex(localDef.getIndexFor(this)) {
                        exprList.guessTypeAt(context)
                    }
                }
            }

            //anonymous
            if (type !is ITyPrimitive)
                type = type.union(TyClass.createAnonymousType(this))
            else if (type == Ty.TABLE)
                type = type.union(TyClass.createAnonymousType(this))
        }
    }
    return type
}

private fun LuaDocTagField.infer(): ITy {
    val stub = stub
    if (stub != null)
        return stub.type
    return ty?.getType() ?: Ty.UNKNOWN
}

@Suppress("UNUSED_PARAMETER")
private fun LuaFuncBodyOwner.infer(context: SearchContext): ITy {
    if (this is LuaFuncDef)
        return TyPsiFunction(false, this, TyFlags.GLOBAL)
    return if (this is LuaClassMethodDef) {
        TyPsiFunction(!this.isStatic, this, 0)
    } else TyPsiFunction(false, this, 0)
}

private fun LuaTableField.infer(context: SearchContext): ITy {
    val stub = stub
    //from comment
    val docTy = if (stub != null) stub.docTy else comment?.docTy
    if (docTy != null)
        return docTy

    //guess from value
    val valueExpr = valueExpr
    return if (valueExpr != null) infer(valueExpr, context) else Ty.UNKNOWN
}

private fun inferFile(file: LuaPsiFile, context: SearchContext): ITy {
    return recursionGuard(file, Computable {
        val moduleName = file.moduleName
        if (moduleName != null)
            TyLazyClass(moduleName)
        else {
            val stub = file.stub
            if (stub != null) {
                val statStub = stub.childrenStubs.lastOrNull { it.psi is LuaReturnStat }
                val stat = statStub?.psi
                if (stat is LuaReturnStat)
                    guessReturnType(stat, 0, context)
                else null
            } else {
                val lastChild = file.lastChild
                var stat: LuaReturnStat? = null
                LuaPsiTreeUtil.walkTopLevelInFile(lastChild, LuaReturnStat::class.java) {
                    stat = it
                    false
                }
                if (stat != null)
                    guessReturnType(stat, 0, context)
                else null
            }
        }
    }) ?: Ty.UNKNOWN
}

/**
 * 找参数的类型
 * @param paramNameDef param name
 * *
 * @param context SearchContext
 * *
 * @return LuaType
 */
private fun resolveParamType(paramNameDef: LuaParamNameDef, context: SearchContext): ITy {
    val paramName = paramNameDef.name
    val paramOwner = PsiTreeUtil.getParentOfType(paramNameDef, LuaParametersOwner::class.java)

    val stub = paramNameDef.stub
    val docTy:ITy? = if (stub != null) { stub.docTy } else {
        // from comment
        val commentOwner = PsiTreeUtil.getParentOfType(paramNameDef, LuaCommentOwner::class.java)
        if (commentOwner != null) {
            val docTy = commentOwner.comment?.getParamDef(paramName)?.type
            if (docTy != null)
                return docTy
        }
        null
    }
    if (docTy != null)
        return docTy

    // 如果是个类方法，则有可能在父类里
    if (paramOwner is LuaClassMethodDef) {
        val classType = paramOwner.guessClassType(context)
        val methodName = paramOwner.name
        var set: ITy = Ty.UNKNOWN
        if (classType != null && methodName != null) {
            TyClass.processSuperClass(classType, context) { superType ->
                val superMethod = superType.findMember(methodName, context)
                if (superMethod is LuaClassMethod) {
                    val params = superMethod.params//todo : 优化
                    for (param in params) {
                        if (paramName == param.name) {
                            set = param.ty
                            if (set != Ty.UNKNOWN)
                                return@processSuperClass false
                        }
                    }
                }
                true
            }
        }
        if (set !is TyUnknown)
            return set
    }

    // module fun
    // function method(self) end
    if (paramOwner is LuaFuncDef && paramName == Constants.WORD_SELF) {
        val moduleName = paramNameDef.moduleName
        if (moduleName != null) {
            return TyLazyClass(moduleName)
        }
    }

    //for
    if (paramOwner is LuaForBStat) {
        val exprList = paramOwner.exprList
        val callExpr = PsiTreeUtil.findChildOfType(exprList, LuaCallExpr::class.java)
        val paramIndex = paramOwner.getIndexFor(paramNameDef)

        // iterator support
        val type = callExpr?.guessType(context)
        if (type != null) {
            var result: ITy = Ty.UNKNOWN
            TyUnion.each(type) {
                if (it is ITyFunction) {
                    val returnTy = it.mainSignature.returnTy
                    if (returnTy is TyTuple) {
                        result = result.union(returnTy.list.getOrElse(paramIndex) { Ty.UNKNOWN })
                    } else if (paramIndex == 0) {
                        result = result.union(returnTy)
                    }
                }
            }
            if (result != Ty.UNKNOWN)
                return result
        }
    }
    // for param = 1, 2 do end
    if (paramOwner is LuaForAStat)
        return Ty.NUMBER
    /**
     * ---@param processor fun(p1:TYPE):void
     * local function test(processor)
     * end
     *
     * test(function(p1)  end)
     *
     * guess type for p1
     */
    if (paramOwner is LuaClosureExpr) {
        var ret: ITy = Ty.UNKNOWN
        val shouldBe = paramOwner.shouldBe(context)
        shouldBe.each {
            if (it is ITyFunction) {
                val paramIndex = paramOwner.getIndexFor(paramNameDef)
                ret = ret.union(it.mainSignature.getParamTy(paramIndex))
            }
        }
        return ret
    }
    return Ty.UNKNOWN
}
