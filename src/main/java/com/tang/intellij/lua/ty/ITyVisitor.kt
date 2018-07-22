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

interface ITyVisitor {
    fun visitTy(ty: ITy)

    fun visitClass(clazz: ITyClass)

    fun visitFun(f: ITyFunction)

    fun visitUnion(u: TyUnion)

    fun visitTuple(tuple: TyTuple)

    fun visitArray(array: ITyArray)

    fun visitGeneric(generic: ITyGeneric)
}

open class TyVisitor : ITyVisitor {
    override fun visitTy(ty: ITy) {
        ty.acceptChildren(this)
    }

    override fun visitClass(clazz: ITyClass) {
        visitTy(clazz)
    }

    override fun visitFun(f: ITyFunction) {
        visitTy(f)
    }

    override fun visitUnion(u: TyUnion) {
        visitTy(u)
    }

    override fun visitTuple(tuple: TyTuple) {
        visitTy(tuple)
    }

    override fun visitArray(array: ITyArray) {
        visitTy(array)
    }

    override fun visitGeneric(generic: ITyGeneric) {
        visitTy(generic)
    }
}