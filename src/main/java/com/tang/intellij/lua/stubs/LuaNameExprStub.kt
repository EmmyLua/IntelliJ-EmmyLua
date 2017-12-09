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

import com.intellij.psi.stubs.StubElement
import com.tang.intellij.lua.psi.LuaNameExpr
import com.tang.intellij.lua.psi.resolveLocal
import com.tang.intellij.lua.stubs.types.LuaNameExprType

/**
 * name stub
 * Created by TangZX on 2017/4/12.
 */
interface LuaNameExprStub : StubElement<LuaNameExpr> {
    val name: String
    val module: String
    val isGlobal: Boolean
}

class LuaNameExprStubImpl : LuaStubBase<LuaNameExpr>, LuaNameExprStub {

    override var module: String

    private var _nameExpr: LuaNameExpr? = null
    private var _name: String
    private var _isGlobal:Boolean = false

    constructor(luaNameExpr: LuaNameExpr, module: String, parent: StubElement<*>, elementType: LuaNameExprType)
            : super(parent, elementType) {
        this.module = module
        _nameExpr = luaNameExpr
        _name = luaNameExpr.name
    }

    constructor(name: String, module: String, isGlobal: Boolean, stubElement: StubElement<*>, luaNameType: LuaNameExprType)
            : super(stubElement, luaNameType) {
        this.module = module
        _name = name
        _isGlobal = isGlobal
    }

    private fun checkGlobal(): Boolean {
        if (_nameExpr != null)
            _isGlobal = resolveLocal(_nameExpr!!, null) == null
        return _isGlobal
    }

    override val isGlobal: Boolean
        get() = checkGlobal()

    override val name: String
        get() = _name
}
