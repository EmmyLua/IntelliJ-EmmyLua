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
import com.intellij.psi.PsiReference
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.psi.LuaExpr
import com.tang.intellij.lua.psi.Visibility
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaNameExprStub
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

/**

 * Created by TangZX on 2017/4/12.
 */
abstract class LuaNameExprMixin : StubBasedPsiElementBase<LuaNameExprStub>, LuaExpr, LuaClassField {
    internal constructor(stub: LuaNameExprStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    internal constructor(node: ASTNode) : super(node)

    internal constructor(stub: LuaNameExprStub, nodeType: IElementType, node: ASTNode) : super(stub, nodeType, node)

    override fun getComment(): LuaComment? {
        return com.tang.intellij.lua.psi.getComment(this)
    }

    override fun getReference(): PsiReference? {
        return references.firstOrNull()
    }

    override fun guessParentType(context: SearchContext): ITy {
        //todo: model type
        return Ty.UNKNOWN
    }

    override val visibility: Visibility
        get() = Visibility.PUBLIC
}
