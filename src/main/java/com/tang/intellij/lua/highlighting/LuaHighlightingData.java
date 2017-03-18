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

package com.tang.intellij.lua.highlighting;

import com.intellij.execution.process.ConsoleHighlighter;
import com.intellij.ide.highlighter.custom.CustomHighlighterColors;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

/**
 *
 * Created by TangZX on 2016/11/22.
 */
public class LuaHighlightingData {
    public static TextAttributesKey DOC_COMMENT_TAG =
            TextAttributesKey.createTextAttributesKey("LUA_DOC_TAG", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG);
    public static TextAttributesKey DOC_COMMENT_TAG_VALUE =
            TextAttributesKey.createTextAttributesKey("LUA_DOC_VALUE", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE);
    public static final TextAttributesKey LOCAL_VAR =
            TextAttributesKey.createTextAttributesKey("LUA_LOCAL_VAR", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey PARAMETER =
            TextAttributesKey.createTextAttributesKey("LUA_PARAMETER", CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES);
    public static final TextAttributesKey FIELD =
            TextAttributesKey.createTextAttributesKey("LUA_FIELD", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey GLOBAL_FUNCTION =
            TextAttributesKey.createTextAttributesKey("LUA_GLOBAL_FUNCTION_ID", DefaultLanguageHighlighterColors.STATIC_FIELD);
    public static final TextAttributesKey GLOBAL_VAR =
            TextAttributesKey.createTextAttributesKey("LUA_GLOBAL_VAR", DefaultLanguageHighlighterColors.STATIC_FIELD);
    public static final TextAttributesKey KEYWORD =
            TextAttributesKey.createTextAttributesKey("LUA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey SELF =
            TextAttributesKey.createTextAttributesKey("LUA_SELF", CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES);
    public static final TextAttributesKey LINE_COMMENT =
            TextAttributesKey.createTextAttributesKey("LUA_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey DOC_COMMENT =
            TextAttributesKey.createTextAttributesKey("LUA_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);
    public static final TextAttributesKey NUMBER =
            TextAttributesKey.createTextAttributesKey("LUA_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING =
            TextAttributesKey.createTextAttributesKey("LUA_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey BRACKETS =
            TextAttributesKey.createTextAttributesKey("LUA_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey BRACES =
            TextAttributesKey.createTextAttributesKey("LUA_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey PARENTHESES =
            TextAttributesKey.createTextAttributesKey("LUA_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey OPERATORS =
            TextAttributesKey.createTextAttributesKey("LUA_OPERATORS", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey SEMICOLON =
            TextAttributesKey.createTextAttributesKey("LUA_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey COMMA =
            TextAttributesKey.createTextAttributesKey("LUA_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey PRIMITIVE_TYPE =
            TextAttributesKey.createTextAttributesKey("LUA_PRIMITIVE_TYPE", ConsoleHighlighter.CYAN_BRIGHT);
    public static TextAttributesKey UP_VALUE =
            TextAttributesKey.createTextAttributesKey("LUA_UP_VALUE");
    public static TextAttributesKey STD_API =
            TextAttributesKey.createTextAttributesKey("LUA_STD_API");
}