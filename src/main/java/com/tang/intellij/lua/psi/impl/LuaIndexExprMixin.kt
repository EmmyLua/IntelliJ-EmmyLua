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
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.intellij.util.Processor
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaIndexStub
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex
import com.tang.intellij.lua.ty.*

/**

 * Created by TangZX on 2017/4/12.
 */
open class LuaIndexExprMixin : StubBasedPsiElementBase<LuaIndexStub>, LuaExpr, LuaClassField {

    internal constructor(stub: LuaIndexStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    internal constructor(node: ASTNode) : super(node)

    internal constructor(stub: LuaIndexStub, nodeType: IElementType, node: ASTNode) : super(stub, nodeType, node)


    override fun getReferences(): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this, PsiReferenceService.Hints.NO_HINTS)
    }

    override fun getReference(): PsiReference? {
        val references = references

        if (references.isNotEmpty())
            return references[0]
        return null
    }

    override fun guessType(context: SearchContext): ITy {
        val retTy = RecursionManager.doPreventingRecursion(this, true) {
            val indexExpr = this as LuaIndexExpr
            // xxx[yyy]
            if (indexExpr.lbrack != null) {
                val tySet = indexExpr.guessPrefixType(context)

                // Type[]
                val array = TyUnion.find(tySet, ITyArray::class.java)
                if (array != null)
                    return@doPreventingRecursion array.base

                // table<number, Type>
                val table = TyUnion.find(tySet, ITyGeneric::class.java)
                if (table != null)
                    return@doPreventingRecursion table.getParamTy(1)

                return@doPreventingRecursion Ty.UNKNOWN
            }

            //from @type annotation
            val comment = this.comment
            if (comment != null) {
                val set = comment.typeDef?.guessType(context)
                if (set != null)
                    return@doPreventingRecursion set
            }

            //guess from value
            var result: ITy = Ty.UNKNOWN
            // value type
            val stub = indexExpr.stub
            val valueTy: ITy?
            if (stub != null)
                valueTy = stub.guessValueType()
            else
                valueTy = indexExpr.guessValueType(context)

            result = result.union(valueTy)

            val propName = this.fieldName
            if (propName != null) {
                val prefixType = indexExpr.guessPrefixType(context)
                TyUnion.each(prefixType) {
                    if (it is TyClass) {
                        result = result.union(guessFieldType(propName, it, context))
                    }
                }
            }
            result
        }

        return retTy ?: Ty.UNKNOWN
    }

    private fun guessFieldType(fieldName: String, type: ITyClass, context: SearchContext): ITy {
        var set:ITy = Ty.UNKNOWN

        //todo: use findField()
        /*val all = LuaClassFieldIndex.findAll(type, fieldName, context)
        for (fieldDef in all) {
            if (fieldDef is LuaIndexExpr) {
                val stub = fieldDef.stub
                set = if (stub != null)
                    set.union(stub.guessValueType())
                else
                    set.union(fieldDef.guessValueType(context))

                if (fieldDef === this)
                    return set
            }

            set = set.union(fieldDef.guessType(context))
        }*/

        LuaClassFieldIndex.processAll(type, fieldName, context, Processor { fieldDef->
            if (fieldDef is LuaIndexExpr) {
                val stub = fieldDef.stub
                set = if (stub != null)
                    set.union(stub.guessValueType())
                else
                    set.union(fieldDef.guessValueType(context))

                if (fieldDef === this)
                    return@Processor false
            }
            set = set.union(fieldDef.guessType(context))
            true
        })

        //class method
        val method = type.findMethod(fieldName, context)
        if (method != null)
            set = set.union(method.asTy(context))

        return set
    }

    override fun getFieldName(): String? {
        val stub = stub
        if (stub != null)
            return stub.fieldName
        return name
    }

    /**
     * --- some comment
     * ---@type type @ annotations
     * self.field = value
     *
     * get comment for `field`
     */
    val comment: LuaComment? get() {
        val p = parent
        if (p is LuaVarList) {
            val stat = p.parent as LuaStatement
            return stat.comment
        }
        return null
    }
}
