package com.tang.intellij.lua.psi.impl;

import com.intellij.lang.ASTNode;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.psi.LuaDeclaration;
import org.jetbrains.annotations.NotNull;

/**
 * 定义基类
 * Created by TangZX on 2016/11/24.
 */
public class LuaDeclarationImpl extends LuaPsiElementImpl implements LuaDeclaration, LuaCommentOwner {
    public LuaDeclarationImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public LuaComment getComment() {
        return LuaCommentUtil.findComment(this);
    }
}
