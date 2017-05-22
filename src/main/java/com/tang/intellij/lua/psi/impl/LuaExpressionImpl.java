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

package com.tang.intellij.lua.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.lang.type.LuaTableType;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

/**
 * 表达式基类
 * Created by TangZX on 2016/12/4.
 */
public class LuaExpressionImpl extends LuaPsiElementImpl implements LuaExpression {
    LuaExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public LuaTypeSet guessType(SearchContext context) {
        LuaTypeSet set = null;
        if (context.push(this, SearchContext.Overflow.GuessType)) {
            if (this instanceof LuaValueExpr)
                set = guessType((LuaValueExpr) this, context);
            if (this instanceof LuaCallExpr)
                set = guessType((LuaCallExpr) this, context);
            if (this instanceof LuaParenExpr)
                set = guessType((LuaParenExpr) this, context);
            else if (this instanceof LuaLiteralExpr)
                set = guessType((LuaLiteralExpr)this, context);
            context.pop(this);
        }
        return set;
    }

    private LuaTypeSet guessType(LuaLiteralExpr literalExpr, SearchContext context) {
        PsiElement child = literalExpr.getFirstChild();
        IElementType type = child.getNode().getElementType();
        if (type == LuaTypes.TRUE || type == LuaTypes.FALSE)
            return LuaTypeSet.create(LuaType.BOOLEAN);
        if (type == LuaTypes.STRING)
            return LuaTypeSet.create(LuaType.STRING);
        if (type == LuaTypes.NUMBER)
            return LuaTypeSet.create(LuaType.NUMBER);
        return null;
    }

    private LuaTypeSet guessType(LuaParenExpr luaParenExpr, SearchContext context) {
        LuaExpr inner = luaParenExpr.getExpr();
        if (inner != null)
            return inner.guessType(context);
        return null;
    }

    private LuaTypeSet guessType(LuaCallExpr luaCallExpr, SearchContext context) {
        // xxx()
        LuaExpr ref = luaCallExpr.getExpr();
        // 从 require 'xxx' 中获取返回类型
        if (ref.textMatches("require")) {
            String filePath = null;
            PsiElement string = luaCallExpr.getFirstStringArg();
            if (string != null) {
                filePath = string.getText();
                filePath = filePath.substring(1, filePath.length() - 1);
            }
            LuaFile file = null;
            if (filePath != null)
                file = LuaPsiResolveUtil.resolveRequireFile(filePath, luaCallExpr.getProject());
            if (file != null)
                return file.getReturnedType(context);
        }
        // find in comment
        LuaFuncBodyOwner bodyOwner = luaCallExpr.resolveFuncBodyOwner(context);
        if (bodyOwner != null)
            return bodyOwner.guessReturnTypeSet(context);
        return null;
    }

    private static LuaTypeSet guessType(LuaValueExpr valueExpr, SearchContext context) {
        PsiElement firstChild = valueExpr.getFirstChild();
        if (firstChild != null) {
            if (firstChild instanceof LuaExpr) {
                return ((LuaExpr) firstChild).guessType(context);
            }
            else if (firstChild instanceof LuaTableConstructor) {
                return LuaTypeSet.create(LuaTableType.create((LuaTableConstructor) firstChild));
            }
            else if (firstChild instanceof LuaVar) {
                LuaVar luaVar = (LuaVar) firstChild;
                return luaVar.getExpr().guessType(context);
            }
        }
        return null;
    }
}
