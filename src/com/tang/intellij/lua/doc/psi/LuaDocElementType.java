package com.tang.intellij.lua.doc.psi;

import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.lang.LuaLanguage;

/**
 * Created by Tangzx on 2016/11/21.
 *
 * @qq 272669294
 */
public class LuaDocElementType extends IElementType {
    public LuaDocElementType(String debugName) {
        super(debugName, LuaLanguage.INSTANCE);
    }
}
