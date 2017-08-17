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
import com.tang.intellij.lua.psi.LuaExpression
import com.tang.intellij.lua.psi.LuaTableExpr
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaTableStub
import com.tang.intellij.lua.ty.TySet
import com.tang.intellij.lua.ty.TyTable

/**

 * Created by Administrator on 2017/6/21.
 */
open class LuaTableExprMixin : StubBasedPsiElementBase<LuaTableStub>, LuaExpression {
    constructor(stub: LuaTableStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    constructor(node: ASTNode) : super(node)

    constructor(stub: LuaTableStub, nodeType: IElementType, node: ASTNode) : super(stub, nodeType, node)

    override fun guessType(context: SearchContext): TySet {
        return TySet.create(TyTable(this as LuaTableExpr))
    }
}
