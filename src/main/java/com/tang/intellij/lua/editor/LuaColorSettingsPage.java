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

package com.tang.intellij.lua.editor;

import com.intellij.codeHighlighting.RainbowHighlighter;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.tang.intellij.lua.highlighting.LuaHighlightingData;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.LuaLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Color Settings Page
 * Created by TangZX on 2017/1/9.
 */
public class LuaColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] ourAttributeDescriptors = new AttributesDescriptor[] {
            new AttributesDescriptor("Keywords", LuaHighlightingData.KEYWORD),
            new AttributesDescriptor("self", LuaHighlightingData.SELF),
            new AttributesDescriptor("String", LuaHighlightingData.STRING),
            new AttributesDescriptor("nil/true/false", LuaHighlightingData.PRIMITIVE_TYPE),
            new AttributesDescriptor("Number", LuaHighlightingData.NUMBER),
            new AttributesDescriptor("Operators", LuaHighlightingData.OPERATORS),
            new AttributesDescriptor("Brackets", LuaHighlightingData.BRACKETS),
            new AttributesDescriptor("Braces", LuaHighlightingData.BRACES),
            new AttributesDescriptor("Parentheses", LuaHighlightingData.PARENTHESES),
            new AttributesDescriptor("Semicolon", LuaHighlightingData.SEMICOLON),
            new AttributesDescriptor("Comma", LuaHighlightingData.COMMA),
            new AttributesDescriptor("Parameters", LuaHighlightingData.PARAMETER),
            new AttributesDescriptor("Line Comment", LuaHighlightingData.LINE_COMMENT),
            new AttributesDescriptor("Doc Comment", LuaHighlightingData.DOC_COMMENT),
            new AttributesDescriptor("Doc Tag", LuaHighlightingData.DOC_COMMENT_TAG),
            new AttributesDescriptor("Doc Tag Value", LuaHighlightingData.DOC_COMMENT_TAG_VALUE),
            new AttributesDescriptor("Local Variables", LuaHighlightingData.LOCAL_VAR),
            new AttributesDescriptor("Global Variables", LuaHighlightingData.GLOBAL_VAR),
            new AttributesDescriptor("Global Functions", LuaHighlightingData.GLOBAL_FUNCTION),
            new AttributesDescriptor("Table Fields", LuaHighlightingData.TABLE_FIELD),
            new AttributesDescriptor("Up Value", LuaHighlightingData.UP_VALUE),
            new AttributesDescriptor("Std api", LuaHighlightingData.STD_API),
    };

    @NonNls
    private static final Map<String, TextAttributesKey> ourTags;
    static {
        ourTags = RainbowHighlighter.createRainbowHLM();
        ourTags.put("Parameters", LuaHighlightingData.PARAMETER);
        ourTags.put("docTag", LuaHighlightingData.DOC_COMMENT_TAG);
        ourTags.put("docTagValue", LuaHighlightingData.DOC_COMMENT_TAG_VALUE);
        ourTags.put("localVar", LuaHighlightingData.LOCAL_VAR);
        ourTags.put("globalVar", LuaHighlightingData.GLOBAL_VAR);
        ourTags.put("globalFunction", LuaHighlightingData.GLOBAL_FUNCTION);
        ourTags.put("tableField", LuaHighlightingData.TABLE_FIELD);
        ourTags.put("localVar", LuaHighlightingData.PARAMETER);
        ourTags.put("upValue", LuaHighlightingData.UP_VALUE);
        ourTags.put("std", LuaHighlightingData.STD_API);
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return LuaIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(LuaLanguage.INSTANCE, null, null);
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "local <localVar>var</localVar> = 1 -- a short comment\n" +
                "--- doc comment\n" +
                "--- @param <docTagValue>par1</docTagValue> Par1Type @some strings\n" +
                "function var:fun(<Parameters>par1</Parameters>, <Parameters>par2</Parameters>)\n" +
                "   <std>print</std>('hello')" +
                "   return self.len + 2\n" +
                "end\n" +
                "\n" +
                "function <globalFunction>globalFun</globalFunction>()\n" +
                "   return \"string\" .. <upValue>var</upValue>\n" +
                "end\n" +
                "\n" +
                "<globalVar>globalVar</globalVar> = {\n" +
                "   <tableField>property</tableField> = value\n" +
                "}\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return ourTags;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ourAttributeDescriptors;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return new ColorDescriptor[0];
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Lua";
    }
}
