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
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef
import com.tang.intellij.lua.comment.psi.LuaDocReturnDef
import com.tang.intellij.lua.ext.ILuaTypeInfer
import com.tang.intellij.lua.ext.recursionGuard
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaFuncBodyOwnerStub

fun infer(element: LuaTypeGuessable?, context: SearchContext): ITy {
    if (element == null)
        return Ty.UNKNOWN
    return ILuaTypeInfer.infer(element, context)
}

internal fun inferInner(element: LuaTypeGuessable, context: SearchContext): ITy {
    return when (element) {
        is LuaFuncBodyOwner -> element.infer(context)
        is LuaExpr -> inferExpr(element, context)
        is LuaParamNameDef -> element.infer(context)
        is LuaNameDef -> element.infer(context)
        is LuaDocFieldDef -> element.infer()
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
            val returnDef = PsiTreeUtil.findChildOfType(comment, LuaDocReturnDef::class.java)
            if (returnDef != null) {
                return returnDef.resolveTypeAt(searchContext.index)
            }
        }
    }

    //infer from return stat
    return recursionGuard(owner, Computable {
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

            override fun visitFuncBodyOwner(o: LuaFuncBodyOwner) { }

            override fun visitClosureExpr(o: LuaClosureExpr) { }
        })
        CachedValueProvider.Result.create(type, owner)
        type
    }) ?: Ty.UNKNOWN
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
    val parent = this.parent
    if (parent is LuaTableField) {
        val expr = PsiTreeUtil.findChildOfType(parent, LuaExpr::class.java)
        if (expr != null)
            type = infer(expr, context)
    } else {
        val docTy = this.docTy
        if (docTy != null)
            return docTy

        val localDef = PsiTreeUtil.getParentOfType(this, LuaLocalDef::class.java)
        if (localDef != null) {
            //计算 expr 返回类型
            if (Ty.isInvalid(type)) {
                val nameList = localDef.nameList
                val exprList = localDef.exprList
                if (nameList != null && exprList != null) {
                    context.index = localDef.getIndexFor(this)
                    type = exprList.guessTypeAt(context)
                }
            }

            //anonymous
            type = type.union(TyClass.createAnonymousType(this))
        }
    }
    return type
}

private fun LuaDocFieldDef.infer(): ITy {
    val stub = stub
    if (stub != null)
        return stub.type
    return ty?.getType() ?: Ty.UNKNOWN
}

private fun LuaFuncBodyOwner.infer(context: SearchContext): ITy {
    if (this is LuaFuncDef)
        return TyPsiFunction(false, this, context, TyFlags.GLOBAL)
    return if (this is LuaClassMethodDef) {
        TyPsiFunction(!this.isStatic, this, context, 0)
    } else TyPsiFunction(false, this, context, 0)
}

private fun LuaTableField.infer(context: SearchContext): ITy {
    val stub = stub
    //from comment
    val docTy = if (stub != null) stub.docTy else comment?.docTy
    if (docTy != null)
        return docTy

    //guess from value
    val valueExpr = PsiTreeUtil.getStubChildOfType(this, LuaExpr::class.java)
    if (valueExpr != null) {
        return infer(valueExpr, context)
    }
    return Ty.UNKNOWN
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
                LuaPsiTreeUtil.walkTopLevelInFile(lastChild, LuaReturnStat::class.java, {
                    stat = it
                    false
                })
                if (stat != null)
                    guessReturnType(stat, 0, context)
                else null
            }
        }
    }) ?: Ty.UNKNOWN
}