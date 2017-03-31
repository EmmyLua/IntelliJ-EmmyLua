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
import com.intellij.psi.util.PsiTreeUtil;
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
        if (this instanceof LuaValueExpr)
            return guessType((LuaValueExpr) this, context);
        if (this instanceof LuaCallExpr)
            return guessType((LuaCallExpr) this, context);
        if (this instanceof LuaIndexExpr)
            return guessType((LuaIndexExpr) this, context);

        return null;
    }

    private LuaTypeSet guessType(LuaIndexExpr indexExpr, SearchContext context) {
        PsiElement id = indexExpr.getId();
        if (id == null) return null;

        LuaTypeSet prefixType = indexExpr.guessPrefixType(context);
        if (prefixType != null && !prefixType.isEmpty()) {
            String propName = id.getText();
            for (LuaType type : prefixType.getTypes()) {
                if (type instanceof LuaTableType) {
                    LuaTableType table = (LuaTableType) type;
                    LuaTableField field = table.tableConstructor.findField(propName);
                    if (field != null) {
                        LuaExpr expr = PsiTreeUtil.findChildOfType(field, LuaExpr.class);
                        if (expr != null) return expr.guessType(context);
                    }
                } else {
                    LuaTypeSet typeSet = type.guessFieldType(propName, context);
                    if (typeSet != null)
                        return typeSet;
                }
            }
        }
        return null;
    }

    private LuaTypeSet guessType(LuaCallExpr luaCallExpr, SearchContext context) {
        // xxx()
        LuaExpr ref = luaCallExpr.getExpr();
        if (ref != null) {
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
        }
        // find in comment
        LuaFuncBodyOwner bodyOwner = luaCallExpr.resolveFuncBodyOwner(context);
        if (bodyOwner != null)
            return bodyOwner.guessReturnTypeSet(context);
        return null;
    }

    private static LuaTypeSet guessType(LuaValueExpr valueExpr, SearchContext context) {
        context = SearchContext.wrapDeadLock(context, SearchContext.TYPE_VALUE_EXPR, valueExpr);
        if (context.isDeadLock(1))
            return null;

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
                LuaNameRef ref = luaVar.getNameRef();
                if (ref != null)
                    return ref.guessType(context);
                else if (luaVar.getExpr() != null)
                    return luaVar.getExpr().guessType(context);
            }
        }
        return null;
    }
}
