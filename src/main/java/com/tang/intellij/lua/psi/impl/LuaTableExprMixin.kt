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
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.psi.LuaExpr
import com.tang.intellij.lua.psi.LuaTableExpr
import com.tang.intellij.lua.psi.LuaTableField
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaTableStub
import com.tang.intellij.lua.ty.*

/**

 * Created by Administrator on 2017/6/21.
 */
open class LuaTableExprMixin : StubBasedPsiElementBase<LuaTableStub>, LuaExpr {
    constructor(stub: LuaTableStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    constructor(node: ASTNode) : super(node)

    constructor(stub: LuaTableStub, nodeType: IElementType, node: ASTNode) : super(stub, nodeType, node)

    override fun guessType(context: SearchContext): Ty {
        val table = this as LuaTableExpr
        // Check for list syntax {a,b,c}
        val isList = table.tableFieldList.size > 0 && table.tableFieldList.all { field -> field.exprList.size == 1 }

        // Resolve type
        if (isList) {
            var baseType : ITy = Ty.UNKNOWN
            for (field in table.tableFieldList) {
                baseType = TyUnion.union(baseType, field.guessType(context))
            }
            return TyArray(baseType)
        }

        // Otherwise return table
        return TyTable(table)
    }
}
