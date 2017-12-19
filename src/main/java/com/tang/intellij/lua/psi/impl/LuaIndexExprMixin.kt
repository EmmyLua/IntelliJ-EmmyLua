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

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.comment.psi.LuaDocAccessModifier
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.stubs.LuaIndexExprStub

/**

 * Created by TangZX on 2017/4/12.
 */
abstract class LuaIndexExprMixin : LuaExprStubMixin<LuaIndexExprStub>, LuaExpr, LuaClassField {

    internal constructor(stub: LuaIndexExprStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    internal constructor(node: ASTNode) : super(node)

    internal constructor(stub: LuaIndexExprStub, nodeType: IElementType, node: ASTNode) : super(stub, nodeType, node)

    override fun getReference(): PsiReference? {
        return references.firstOrNull()
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

    override val visibility: Visibility get() {
        val stub = this.stub
        if (stub != null)
            return stub.visibility
        return comment?.findTag(LuaDocAccessModifier::class.java)?.let {
            Visibility.get(it.text)
        } ?: Visibility.PUBLIC
    }
}
