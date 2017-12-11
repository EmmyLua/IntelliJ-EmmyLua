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
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaNameExprStub
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyClass
import com.tang.intellij.lua.ty.TySerializedClass

/**

 * Created by TangZX on 2017/4/12.
 */
abstract class LuaNameExprMixin : StubBasedPsiElementBase<LuaNameExprStub>, LuaExpr, LuaClassField {
    internal constructor(stub: LuaNameExprStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    internal constructor(node: ASTNode) : super(node)

    internal constructor(stub: LuaNameExprStub, nodeType: IElementType, node: ASTNode) : super(stub, nodeType, node)

    override fun getReference(): PsiReference? {
        return references.firstOrNull()
    }

    override fun guessParentType(context: SearchContext): ITy {
        //todo: model type
        return Ty.UNKNOWN
    }

    override fun guessType(context: SearchContext): ITy {
        val set = RecursionManager.doPreventingRecursion(this, true) {
            var type:ITy = Ty.UNKNOWN
            val nameExpr = this as LuaNameExpr

            val multiResolve = multiResolve(nameExpr, context)
            multiResolve.forEach {
                val set = getType(context, it)
                type = type.union(set)
            }

            if (Ty.isInvalid(type)) {
                type = type.union(getType(context, nameExpr))
            }

            type
        }
        return set ?: Ty.UNKNOWN
    }

    private fun getType(context: SearchContext, def: PsiElement): ITy {
        when (def) {
            is LuaNameExpr -> {
                //todo stub.module -> ty
                val stub = def.stub
                stub?.module?.let {
                    val memberType = TySerializedClass(it).findMemberType(def.name, context)
                    if (memberType != null && !Ty.isInvalid(memberType))
                        return memberType
                }

                var type: ITy = def.docTy ?: Ty.UNKNOWN
                //guess from value expr
                if (Ty.isInvalid(type)) {
                    val stat = def.assignStat
                    if (stat != null) {
                        val exprList = stat.valueExprList
                        if (exprList != null) {
                            context.index = stat.getIndexFor(def)
                            type = exprList.guessTypeAt(context)
                        }
                    }
                }
                /*val stat = def.assignStat
                if (stat != null) {
                    val comment = stat.comment
                    //guess from comment
                    if (comment != null)
                        type = comment.guessType(context)
                    //guess from value expr
                    if (Ty.isInvalid(type)) {
                        val exprList = stat.valueExprList
                        if (exprList != null) {
                            context.index = stat.getIndexFor(def)
                            type = exprList.guessTypeAt(context)
                        }
                    }
                }*/

                //Global
                if (isGlobal(def)) {
                    //use globalClassTy to store class members, that's very important
                    type = type.union(TyClass.createGlobalType(def))
                }
                return type
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

    override val visibility: Visibility
        get() = Visibility.PUBLIC
}
