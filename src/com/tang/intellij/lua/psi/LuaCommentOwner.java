package com.tang.intellij.lua.psi;

import com.tang.intellij.lua.doc.psi.api.LuaComment;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public interface LuaCommentOwner extends LuaPsiElement {
    LuaComment getComment();
}
