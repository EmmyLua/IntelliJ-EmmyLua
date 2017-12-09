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

package com.tang.intellij.lua.psi

import com.tang.intellij.lua.psi.impl.*
import com.tang.intellij.lua.stubs.LuaPlaceholderStub

object LuaElementTypes {
    val LOCAL_DEF = LuaPlaceholderStub.Type("LOCAL_DEF", ::LuaLocalDefImpl)
    val CALL_EXPR = LuaPlaceholderStub.Type("EXPR_LIST", ::LuaExprListImpl)
    val EXPR_LIST = LuaPlaceholderStub.Type("EXPR_LIST", ::LuaExprListImpl)
    val NAME_LIST = LuaPlaceholderStub.Type("NAME_LIST", ::LuaNameListImpl)
    val ASSIGN_STAT = LuaPlaceholderStub.Type("ASSIGN_STAT", ::LuaAssignStatImpl)
    val VAR_LIST = LuaPlaceholderStub.Type("VAR_LIST", ::LuaVarListImpl)
}