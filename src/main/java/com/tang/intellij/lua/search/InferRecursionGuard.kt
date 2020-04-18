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

package com.tang.intellij.lua.search

import com.intellij.psi.PsiElement
import com.tang.intellij.lua.psi.LuaNameExpr

enum class GuardType {
    Unknown,
    GlobalName,
    RecursionCall,
    Inference
}

open class InferRecursionGuard(val psi: PsiElement) {
    open fun check(psi: PsiElement, type: GuardType): Boolean {
        return this.psi == psi
    }
}

fun createGuard(psi: PsiElement, type: GuardType): InferRecursionGuard? {
    if (type == GuardType.GlobalName && psi is LuaNameExpr) {
        return GlobalSearchGuard(psi)
    }
    return InferRecursionGuard(psi)
}

class GlobalSearchGuard(psi: LuaNameExpr) : InferRecursionGuard(psi) {
    private val name = psi.name

    override fun check(psi: PsiElement, type: GuardType): Boolean {
        if (type == GuardType.GlobalName && psi is LuaNameExpr && psi.name == name) {
            //println("guard global name: $name")
            return true
        }
        return super.check(psi, type)
    }
}