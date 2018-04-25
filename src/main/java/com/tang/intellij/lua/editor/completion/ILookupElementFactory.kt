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

package com.tang.intellij.lua.editor.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.psi.LuaPsiElement
import com.tang.intellij.lua.ty.IFunSignature
import com.tang.intellij.lua.ty.ITyFunction
import javax.swing.Icon

interface ILookupElementFactory {
    fun createFunctionLookupElement(name: String,
                                    psi: LuaPsiElement,
                                    signature: IFunSignature,
                                    bold: Boolean,
                                    colonStyle: Boolean,
                                    ty: ITyFunction,
                                    icon: Icon): LookupElement

    fun createFieldLookupElement(fieldName: String, field: LuaClassField, bold: Boolean): LookupElement
}