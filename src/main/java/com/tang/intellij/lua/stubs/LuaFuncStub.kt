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

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.tang.intellij.lua.psi.LuaFuncDef
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.ty.ITyFunction

/**

 * Created by tangzx on 2016/11/26.
 */
interface LuaFuncStub : LuaFuncBodyOwnerStub<LuaFuncDef> {
    val name: String
    val module: String
}

class LuaFuncStubImpl(override val name: String,
                      override val module: String,
                      override val ty: ITyFunction,
                      parent: StubElement<*>)
    : StubBase<LuaFuncDef>(parent, LuaTypes.FUNC_DEF as IStubElementType<*, *>), LuaFuncStub