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

package com.tang.intellij.lua.spellchecker

import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.PlainTextSplitter
import com.intellij.spellchecker.tokenizer.EscapeSequenceTokenizer
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.TokenConsumer
import com.intellij.spellchecker.tokenizer.Tokenizer
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaLiteralExpr
import com.tang.intellij.lua.psi.LuaLiteralKind
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.psi.kind

class LuaSpellcheckingStrategy : SpellcheckingStrategy() {
    override fun isMyContext(element: PsiElement): Boolean {
        return element.language is LuaLanguage
    }

    override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
        if (element?.node?.elementType == LuaTypes.LITERAL_EXPR)
            return StringLiteralTokenizer
        return super.getTokenizer(element)
    }
}

object StringLiteralTokenizer : EscapeSequenceTokenizer<LuaLiteralExpr>() {

    override fun tokenize(element: LuaLiteralExpr, consumer: TokenConsumer) {
        if (element.kind == LuaLiteralKind.String) {
            consumer.consumeToken(element, PlainTextSplitter.getInstance())
        }
    }
}