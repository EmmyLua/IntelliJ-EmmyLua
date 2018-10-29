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

package com.tang.intellij.lua.stubs.index

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.stubs.StubIndexKey
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.psi.LuaClassMember

object StubKeys {
    val CLASS_MEMBER = StubIndexKey.createIndexKey<Int, LuaClassMember>("lua.index.class.member")
    val SHORT_NAME = StubIndexKey.createIndexKey<String, NavigatablePsiElement>("lua.index.short_name")
    val CLASS = StubIndexKey.createIndexKey<String, LuaDocTagClass>("lua.index.class")
    val SUPER_CLASS = StubIndexKey.createIndexKey<String, LuaDocTagClass>("lua.index.super_class")
}
