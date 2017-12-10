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
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.psi.Visibility
import com.tang.intellij.lua.ty.ITy

/**

 * Created by tangzx on 2016/12/10.
 */
interface LuaDocClassFieldStub : LuaClassMemberStub<LuaDocFieldDef> {
    val name: String

    val type: ITy

    val className: String?
}

class LuaDocClassFieldStubImpl(parent: StubElement<*>,
                               override val name: String,
                               override val className: String?,
                               override val visibility: Visibility,
                               override val type: ITy)
    : LuaDocStubBase<LuaDocFieldDef>(parent, LuaElementType.CLASS_FIELD_DEF), LuaDocClassFieldStub {
    override val docTy: ITy?
        get() = null
}