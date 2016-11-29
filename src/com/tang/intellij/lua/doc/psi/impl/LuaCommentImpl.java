package com.tang.intellij.lua.doc.psi.impl;

import com.intellij.psi.impl.source.tree.LazyParseablePsiElement;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.doc.LuaCommentUtil;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.psi.LuaTypes;

/**
 * Created by Tangzx on 2016/11/21.
 *
 * @qq 272669294
 */
public class LuaCommentImpl extends LazyParseablePsiElement implements LuaComment {

    public LuaCommentImpl(CharSequence charSequence) {
        super(LuaTypes.DOC_COMMENT, charSequence);
    }

    @Override
    public IElementType getTokenType() {
        return LuaTypes.DOC_COMMENT;
    }

    @Override
    public LuaCommentOwner getOwner() {
        return LuaCommentUtil.findOwner(this);
    }

    @Override
    public String toString() {
        return "DOC_COMMENT";
    }
}
