package com.tang.intellij.lua.codeInsight;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2017/2/11.
 */
public class LuaLiveTemplatesProvider implements DefaultLiveTemplatesProvider {
    private static final String[] DEFAULT_TEMPLATES = {
            "/liveTemplates/lua"
    };

    @Override
    public String[] getDefaultLiveTemplateFiles() {
        return DEFAULT_TEMPLATES;
    }

    @Nullable
    @Override
    public String[] getHiddenLiveTemplateFiles() {
        return new String[0];
    }
}
