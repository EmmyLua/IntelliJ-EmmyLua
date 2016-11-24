package com.tang.intellij.lua.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.tang.intellij.lua.psi.LuaPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * LuaPsiElement 基类
 * Created by TangZX on 2016/11/24.
 */
public class LuaPsiElementImpl extends ASTWrapperPsiElement implements LuaPsiElement {
    public LuaPsiElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}
