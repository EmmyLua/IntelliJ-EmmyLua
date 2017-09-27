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

package com.tang.intellij.lua.editor.structure

import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.LuaClassMethodDef
import com.tang.intellij.lua.psi.LuaFuncDef
import com.tang.intellij.lua.psi.LuaLocalFuncDef
import com.tang.intellij.lua.psi.LuaPsiElement
import javax.swing.Icon

/**
 * Created by TangZX on 2016/12/13.
 */
open class LuaFuncElement private constructor(target:LuaPsiElement, name:String?, paramSignature:String, icon:Icon) : LuaTreeElement(target, icon) {
    internal constructor(target:LuaPsiElement, name:String?, paramSignature:String) : this(target, name, paramSignature, LuaIcons.LOCAL_FUNCTION)
    internal constructor(target:LuaLocalFuncDef) : this(target, target.name, target.paramSignature, LuaIcons.LOCAL_FUNCTION)
    internal constructor(target:LuaFuncDef) : this(target, target.name, target.paramSignature, LuaIcons.GLOBAL_FUNCTION)
    internal constructor(target:LuaClassMethodDef) : this(target, target.name, target.paramSignature, LuaIcons.CLASS_METHOD)

    companion object {
        fun asClassMethod(target:LuaPsiElement, name:String, signature:String):LuaFuncElement {
            return LuaFuncElement(target, name, signature, LuaIcons.CLASS_METHOD)
        }
    }

    internal var name:String = name + paramSignature

    override fun getPresentableText(): String {
        return name
    }
}
