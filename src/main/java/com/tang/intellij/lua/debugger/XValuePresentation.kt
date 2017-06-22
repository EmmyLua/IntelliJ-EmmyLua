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

package com.tang.intellij.lua.debugger

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.xdebugger.frame.presentation.XNumericValuePresentation
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation
import com.intellij.xdebugger.frame.presentation.XValuePresentation

class LuaXValuePresentation(val sType: String, val sValue:String, val tkey : TextAttributesKey? = null) : XValuePresentation() {
    override fun renderValue(renderer: XValueTextRenderer) {
        if (tkey == null) renderer.renderValue(sValue)
        else renderer.renderValue(sValue, tkey)
    }

    override fun getType() = sType
}

class LuaXStringPresentation(sValue: String) : XStringValuePresentation(sValue) {
    override fun getType() = "string"
}

class LuaXNumberPresentation(sValue: String) : XNumericValuePresentation(sValue) {
    override fun renderValue(renderer: XValueTextRenderer) {
        super.renderValue(renderer)
    }

    override fun getType() = "number"
}