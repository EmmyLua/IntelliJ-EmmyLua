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

package com.tang.intellij.lua.highlighting

import com.intellij.execution.process.ConsoleHighlighter
import com.intellij.ide.highlighter.custom.CustomHighlighterColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

/**
 *
 * Created by TangZX on 2016/11/22.
 */
object LuaHighlightingData {
    val DOC_COMMENT_TAG = TextAttributesKey.createTextAttributesKey("LUA_DOC_TAG", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG)
    val DOC_COMMENT_TAG_VALUE = TextAttributesKey.createTextAttributesKey("LUA_DOC_VALUE", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE)
    val DOC_KEYWORD = TextAttributesKey.createTextAttributesKey("LUA_DOC_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val CLASS_NAME = TextAttributesKey.createTextAttributesKey("LUA_CLASS_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
    val CLASS_REFERENCE = TextAttributesKey.createTextAttributesKey("LUA_CLASS_REFERENCE", DefaultLanguageHighlighterColors.CLASS_REFERENCE)

    val LOCAL_VAR = TextAttributesKey.createTextAttributesKey("LUA_LOCAL_VAR", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
    val PARAMETER = TextAttributesKey.createTextAttributesKey("LUA_PARAMETER", CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES)
    val FIELD = TextAttributesKey.createTextAttributesKey("LUA_FIELD")
    val GLOBAL_FUNCTION = TextAttributesKey.createTextAttributesKey("LUA_GLOBAL_FUNCTION_ID", DefaultLanguageHighlighterColors.STATIC_FIELD)
    val GLOBAL_VAR = TextAttributesKey.createTextAttributesKey("LUA_GLOBAL_VAR", DefaultLanguageHighlighterColors.STATIC_FIELD)
    val KEYWORD = TextAttributesKey.createTextAttributesKey("LUA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val SELF = TextAttributesKey.createTextAttributesKey("LUA_SELF", CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES)
    val LINE_COMMENT = TextAttributesKey.createTextAttributesKey("LUA_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val DOC_COMMENT = TextAttributesKey.createTextAttributesKey("LUA_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
    val NUMBER = TextAttributesKey.createTextAttributesKey("LUA_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val STRING = TextAttributesKey.createTextAttributesKey("LUA_STRING", DefaultLanguageHighlighterColors.STRING)
    val BRACKETS = TextAttributesKey.createTextAttributesKey("LUA_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val BRACES = TextAttributesKey.createTextAttributesKey("LUA_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val PARENTHESES = TextAttributesKey.createTextAttributesKey("LUA_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    val DOT = TextAttributesKey.createTextAttributesKey("LUA_DOT", DefaultLanguageHighlighterColors.DOT)
    val OPERATORS = TextAttributesKey.createTextAttributesKey("LUA_OPERATORS", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val SEMICOLON = TextAttributesKey.createTextAttributesKey("LUA_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)
    val COMMA = TextAttributesKey.createTextAttributesKey("LUA_COMMA", DefaultLanguageHighlighterColors.COMMA)
    val PRIMITIVE_TYPE = TextAttributesKey.createTextAttributesKey("LUA_PRIMITIVE_TYPE", ConsoleHighlighter.CYAN_BRIGHT)
    val UP_VALUE = TextAttributesKey.createTextAttributesKey("LUA_UP_VALUE")
    val STD_API = TextAttributesKey.createTextAttributesKey("LUA_STD_API")
    val INSTANCE_METHOD = TextAttributesKey.createTextAttributesKey("LUA_INSTANCE_METHOD", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    val STATIC_METHOD = TextAttributesKey.createTextAttributesKey("LUA_STATIC_METHOD", DefaultLanguageHighlighterColors.STATIC_METHOD)

    //region
    val REGION_HEADER = TextAttributesKey.createTextAttributesKey("LUA_REGION_START", DefaultLanguageHighlighterColors.DOC_COMMENT)
    val REGION_DESC = TextAttributesKey.createTextAttributesKey("LUA_REGION_DESC")
}