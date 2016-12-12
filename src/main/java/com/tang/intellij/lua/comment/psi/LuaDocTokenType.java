package com.tang.intellij.lua.comment.psi;

import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.lang.LuaLanguage;

/**
 * Created by Tangzx on 2016/11/21.
 *
 * @qq 272669294
 */
public class LuaDocTokenType extends IElementType {
    public LuaDocTokenType(String debugName) {
        super(debugName, LuaLanguage.INSTANCE);
    }
}
