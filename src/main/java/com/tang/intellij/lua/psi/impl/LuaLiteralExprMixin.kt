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

package com.tang.intellij.lua.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.tang.intellij.lua.lang.type.LuaString
import com.tang.intellij.lua.psi.LuaElementFactory
import com.tang.intellij.lua.psi.LuaLiteralExpr
import com.tang.intellij.lua.psi.LuaLiteralKind
import com.tang.intellij.lua.psi.kind
import java.lang.StringBuilder

internal class TextEscaper(host: LuaLiteralExprMixin) : LiteralTextEscaper<LuaLiteralExprMixin>(host) {
    override fun isOneLine(): Boolean {
        return true
    }

    override fun decode(textRange: TextRange, builder: StringBuilder): Boolean {
        val content = LuaString.getContent(myHost.text)
        builder.append(content.value)
        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, range: TextRange): Int {
        return offsetInDecoded + range.startOffset
    }

}

open class LuaLiteralExprMixin(node: ASTNode) : LuaExprMixin(node), PsiLanguageInjectionHost {
    override fun updateText(text: String): PsiLanguageInjectionHost {
        val expr = LuaElementFactory.createLiteral(project, text)
        node.replaceChild(node.firstChildNode, expr.node.firstChildNode)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return TextEscaper(this)
    }

    override fun isValidHost(): Boolean {
        val le = this as LuaLiteralExpr
        if (le.kind == LuaLiteralKind.String) {
            val content = LuaString.getContent(le.text)
            return content.start >= 2
        }
        return false
    }
}