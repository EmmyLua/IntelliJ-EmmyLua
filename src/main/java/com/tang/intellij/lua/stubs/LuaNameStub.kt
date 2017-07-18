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

package com.tang.intellij.lua.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.tang.intellij.lua.psi.LuaNameExpr
import com.tang.intellij.lua.psi.resolveLocal
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.types.LuaNameType

/**
 * name stub
 * Created by TangZX on 2017/4/12.
 */
interface LuaNameStub : StubElement<LuaNameExpr> {
    val name: String
    val isGlobal: Boolean
}

class LuaNameStubImpl : StubBase<LuaNameExpr>, LuaNameStub {

    private lateinit var _nameExpr: LuaNameExpr
    private var _name: String

    constructor(luaNameExpr: LuaNameExpr, parent: StubElement<*>, elementType: LuaNameType) : super(parent, elementType) {
        _nameExpr = luaNameExpr
        _name = luaNameExpr.name
    }

    constructor(name: String, stubElement: StubElement<*>, luaNameType: LuaNameType) : super(stubElement, luaNameType) {
        _name = name
    }

    private fun checkGlobal(): Boolean {
        val context = SearchContext(_nameExpr.project)
        context.setCurrentStubFile(_nameExpr.containingFile)
        return resolveLocal(_nameExpr, context) == null
    }

    override val isGlobal: Boolean
        get() = checkGlobal()

    override val name: String
        get() = _name
}
