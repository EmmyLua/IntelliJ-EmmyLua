package com.tang.intellij.lua.editor;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.tang.intellij.lua.highlighting.LuaHighlightingData;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.LuaLanguage;
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
            new AttributesDescriptor("KeyWords", LuaHighlightingData.KEYWORD),
            new AttributesDescriptor("self", LuaHighlightingData.SELF),
            new AttributesDescriptor("String", LuaHighlightingData.STRING),
            new AttributesDescriptor("Number", LuaHighlightingData.NUMBER),
            new AttributesDescriptor("Operators", LuaHighlightingData.OPERATORS),
            new AttributesDescriptor("Parameters", LuaHighlightingData.PARAMETER),
            new AttributesDescriptor("Line Comment", LuaHighlightingData.LINE_COMMENT),
            new AttributesDescriptor("Doc Comment", LuaHighlightingData.DOC_COMMENT),
            new AttributesDescriptor("Doc Tag", LuaHighlightingData.DOC_COMMENT_TAG),
            new AttributesDescriptor("Doc Tag Value", LuaHighlightingData.DOC_COMMENT_TAG_VALUE),
            new AttributesDescriptor("Local Variables", LuaHighlightingData.LOCAL_VAR),
            new AttributesDescriptor("Global Variables", LuaHighlightingData.GLOBAL_VAR),
            new AttributesDescriptor("Global Functions", LuaHighlightingData.GLOBAL_FUNCTION),
            new AttributesDescriptor("Table Fields", LuaHighlightingData.TABLE_FIELD)
    };

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
        return "local var = nil -- a short comment\n" +
                "--- doc comment\n" +
                "--- @param par1 Par1Type @some strings\n" +
                "function var:fun(par1, par2)\n" +
                "   return self.len + 2\n" +
                "end\n" +
                "\n" +
                "function globalFun()\n" +
                "   return \"string\"\n" +
                "end\n" +
                "globalVar = {\n" +
                "   property = value\n" +
                "}\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
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
