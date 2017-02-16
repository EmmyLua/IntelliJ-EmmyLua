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
import org.jetbrains.annotations.NonNls;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LOCAL_VARIABLE;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STATIC_FIELD;

/**
 *
 * Created by TangZX on 2016/11/22.
 */
public class LuaHighlightingData {
    @NonNls
    private static final String KEYWORD_ID = "LUA_KEYWORD";
    @NonNls
    private static final String LINE_COMMENT_ID = "LUA_LINE_COMMENT";
    @NonNls
    private static final String DOC_COMMENT_ID = "LUA_DOC_COMMENT";
    @NonNls
    private static final String NUMBER_ID = "LUA_NUMBER";
    @NonNls
    private static final String STRING_ID = "LUA_STRING";
    @NonNls
    private static final String LONGSTRING_ID = "LUA_LONGSTRING";
    @NonNls
    private static final String LONGSTRING_BRACES_ID = "LUA_LONGSTRING_BRACES";
    @NonNls
    private static final String BRACES_ID = "LUA_BRACES";
    @NonNls
    private static final String BRACKETS_ID = "LUA_BRACKETS";
    @NonNls
    private static final String LOCAL_VAR_ID = "LUA_LOCAL_VAR";
    @NonNls
    private static final String GLOBAL_VAR_ID = "LUA_GLOBAL_VAR";
    @NonNls
    private static final String GLOBAL_FUNCTION_ID = "LUA_GLOBAL_FUNCTION_ID";
    @NonNls
    private static final String TABLE_FIELD_ID = "LUA_FIELD";
    @NonNls
    private static final String PARAMETER_ID = "LUA_PARAMETER";
    @NonNls
    private static final String OPERATORS_ID = "LUA_OPERATORS";
    @NonNls
    private static final String LUADOC_TAG_ID = "LUA_LUADOC_TAG";
    @NonNls
    private static final String LUADOC_VALUE_ID = "LUA_LUADOC_VALUE";
    @NonNls
    private static final String UP_VALUE_ID = "LUA_UP_VALUE";

    public static TextAttributesKey DOC_COMMENT_TAG =
            TextAttributesKey.createTextAttributesKey(LUADOC_TAG_ID, DefaultLanguageHighlighterColors.DOC_COMMENT_TAG);
    public static TextAttributesKey DOC_COMMENT_TAG_VALUE =
            TextAttributesKey.createTextAttributesKey(LUADOC_VALUE_ID, DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE);
    public static final TextAttributesKey LOCAL_VAR =
            TextAttributesKey.createTextAttributesKey(LOCAL_VAR_ID, LOCAL_VARIABLE);
    public static final TextAttributesKey PARAMETER =
            TextAttributesKey.createTextAttributesKey(PARAMETER_ID, CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES);
    public static final TextAttributesKey TABLE_FIELD =
            TextAttributesKey.createTextAttributesKey(TABLE_FIELD_ID, STATIC_FIELD);
    public static final TextAttributesKey GLOBAL_FUNCTION =
            TextAttributesKey.createTextAttributesKey(GLOBAL_FUNCTION_ID, STATIC_FIELD);
    public static final TextAttributesKey GLOBAL_VAR =
            TextAttributesKey.createTextAttributesKey(GLOBAL_VAR_ID, STATIC_FIELD);
    public static final TextAttributesKey KEYWORD =
            TextAttributesKey.createTextAttributesKey(KEYWORD_ID, DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey SELF =
            TextAttributesKey.createTextAttributesKey("SELF", CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES);
    public static final TextAttributesKey LINE_COMMENT =
            TextAttributesKey.createTextAttributesKey(LINE_COMMENT_ID, DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey DOC_COMMENT =
            TextAttributesKey.createTextAttributesKey(DOC_COMMENT_ID, DefaultLanguageHighlighterColors.DOC_COMMENT);
    public static final TextAttributesKey NUMBER =
            TextAttributesKey.createTextAttributesKey(NUMBER_ID, DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING =
            TextAttributesKey.createTextAttributesKey(STRING_ID, DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LONGSTRING =
            TextAttributesKey.createTextAttributesKey(LONGSTRING_ID, DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LONGSTRING_BRACES =
            TextAttributesKey.createTextAttributesKey(LONGSTRING_BRACES_ID, DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey BRACKETS =
            TextAttributesKey.createTextAttributesKey(BRACKETS_ID, DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey BRACES =
            TextAttributesKey.createTextAttributesKey(BRACES_ID, DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey OPERATORS =
            TextAttributesKey.createTextAttributesKey(OPERATORS_ID, DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey PRIMITIVE_TYPE =
            TextAttributesKey.createTextAttributesKey("LUA_PRIMITIVE_TYPE", ConsoleHighlighter.CYAN_BRIGHT);
    public static TextAttributesKey UP_VALUE =
            TextAttributesKey.createTextAttributesKey(UP_VALUE_ID);
}