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

import com.tang.intellij.lua.search.SearchContext

interface ITyPrimitive : ITy {
    val primitiveKind: TyPrimitiveKind
}

// number, boolean, nil, void ...
class TyPrimitive(override val primitiveKind: TyPrimitiveKind,
                  override val displayName: String) : Ty(TyKind.Primitive), ITyPrimitive {

    override val booleanType = if (primitiveKind == TyPrimitiveKind.Boolean) Ty.BOOLEAN else Ty.TRUE

    override fun equals(other: Any?): Boolean {
        return other is TyPrimitive && other.primitiveKind == primitiveKind
    }

    override fun hashCode(): Int {
        return primitiveKind.hashCode()
    }

    override fun contravariantOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        return super.contravariantOf(other, context, strict)
                || (other is ITyPrimitive && other.primitiveKind == primitiveKind)
                || (primitiveKind == TyPrimitiveKind.Table && (other.kind == TyKind.Array || other.kind == TyKind.Class))
                || (primitiveKind == TyPrimitiveKind.Function && other.kind == TyKind.Function)
    }
}

// string
class TyPrimitiveClass(override val primitiveKind: TyPrimitiveKind,
                       override val displayName: String) : TyClass(displayName), ITyPrimitive {

    override val kind = TyKind.Primitive

    override fun getSuperClass(context: SearchContext): ITy? = null

    override fun doLazyInit(searchContext: SearchContext) { }

    override fun equals(other: Any?): Boolean {
        return other is TyPrimitiveClass && other.primitiveKind == primitiveKind
    }

    override fun hashCode(): Int {
        return primitiveKind.hashCode()
    }

    override fun contravariantOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        return super.contravariantOf(other, context, strict)
                || (other is ITyPrimitive && other.primitiveKind == primitiveKind)
    }
}
