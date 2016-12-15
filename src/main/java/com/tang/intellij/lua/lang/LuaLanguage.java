package com.tang.intellij.lua.lang;

import com.intellij.lang.Language;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaLanguage extends Language {

    public static final int INDEX_VERSION = 4;

    public static final LuaLanguage INSTANCE = new LuaLanguage();

    public LuaLanguage() {
        super("Lua");
    }
}
