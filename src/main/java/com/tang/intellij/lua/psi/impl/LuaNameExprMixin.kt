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

package com.tang.intellij.lua.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.RecursionManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaNameStub
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyClass
import com.tang.intellij.lua.ty.TyFlags

/**

 * Created by TangZX on 2017/4/12.
 */
abstract class LuaNameExprMixin : StubBasedPsiElementBase<LuaNameStub>, LuaExpr, LuaClassField {
    internal constructor(stub: LuaNameStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    internal constructor(node: ASTNode) : super(node)

    internal constructor(stub: LuaNameStub, nodeType: IElementType, node: ASTNode) : super(stub, nodeType, node)

    override fun getReference(): PsiReference? {
        return references.firstOrNull()
    }

    override fun guessParentType(context: SearchContext): ITy {
        return Ty.UNKNOWN
    }

    override fun guessType(context: SearchContext): ITy {
        val set = RecursionManager.doPreventingRecursion(this, true) {
            var typeSet:ITy = Ty.UNKNOWN
            val nameExpr = this as LuaNameExpr

            val multiResolve = multiResolve(nameExpr, context)
            multiResolve.forEach {
                val set = getTypeSet(context, it)
                typeSet = typeSet.union(set)
            }

            if (Ty.isInvalid(typeSet)) {
                typeSet = typeSet.union(getTypeSet(context, nameExpr))
            }

            typeSet
        }
        return set ?: Ty.UNKNOWN
    }

    private fun getTypeSet(context: SearchContext, def: PsiElement): ITy {
        when (def) {
            is LuaNameExpr -> {
                var typeSet: ITy = if (!LuaSettings.instance.isEnforceTypeSafety) Ty.UNKNOWN else Ty.NIL
                val p1 = def.parent // should be VAR_LIST
                val p2 = p1.parent // should be ASSIGN_STAT
                if (p2 is LuaAssignStat) {
                    val comment = p2.comment
                    //优先从 Comment 猜
                    if (comment != null)
                        typeSet = comment.guessType(context)
                    //再从赋值猜
                    if (Ty.isInvalid(typeSet)) {
                        val exprList = p2.valueExprList
                        if (exprList != null) {
                            context.index = p2.getIndexFor(def)
                            typeSet = exprList.guessTypeAt(context)
                        }
                    }
                }

                //Global
                if (isGlobal(def)) {
                    if (!LuaSettings.instance.isEnforceTypeSafety) typeSet = typeSet.union(TyClass.createGlobalType(def))
                    if (typeSet is Ty) typeSet.addFlag(TyFlags.GLOBAL)
                }
                return typeSet
            }
            is LuaTypeGuessable -> return def.guessTypeFromCache(context)
            else -> return Ty.UNKNOWN
        }
    }

    private fun isGlobal(nameExpr: LuaNameExpr):Boolean {
        val minx = nameExpr as LuaNameExprMixin
        val gs = minx.greenStub
        return gs?.isGlobal ?: (resolveLocal(nameExpr, null) == null)
    }
}
