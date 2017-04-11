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

package com.tang.intellij.lua.stubs.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaVarStub;

/**
 *
 * Created by tangzx on 2017/1/12.
 */
public class LuaVarStubImpl extends StubBase<LuaVar> implements LuaVarStub {

    private LuaIndexExpr indexExpr;
    private String typeName;
    private String fieldName;
    private boolean isGlobal;
    private boolean isValid;

    public LuaVarStubImpl(StubElement parent,
                          IStubElementType elementType) {
        super(parent, elementType);
        this.isValid = false;
        this.isGlobal = false;
    }

    public LuaVarStubImpl(StubElement parent,
                          IStubElementType elementType,
                          LuaVar var) {
        super(parent, elementType);
        this.isValid = checkValid(var);

        if (this.isValid) {
            LuaExpr expr = var.getExpr();
            if (expr instanceof LuaNameExpr) {
                this.isGlobal = true;
                this.fieldName = expr.getText();
            } else {
                this.isGlobal = false;
                LuaIndexExpr indexExpr = (LuaIndexExpr) expr;
                assert indexExpr.getId() != null;
                this.indexExpr = indexExpr;
            }
        }
    }

    public LuaVarStubImpl(StubElement stubElement,
                          IStubElementType type,
                          String typeName,
                          String fieldName) {
        super(stubElement, type);
        this.isGlobal = false;
        this.typeName = typeName;
        this.fieldName = fieldName;
    }

    public LuaVarStubImpl(StubElement stubElement,
                          IStubElementType type,
                          String fieldName) {
        super(stubElement, type);
        this.isGlobal = true;
        this.fieldName = fieldName;
    }

    public boolean isValid() {
        return isValid;
    }

    private boolean checkValid(LuaVar var) {
        LuaAssignStat assignStat = PsiTreeUtil.getParentOfType(var, LuaAssignStat.class);
        assert assignStat != null;
        if (assignStat.getExprList() == null) // 确定是XXX.XX = XXX 完整形式
            return false;

        LuaExpr expr = var.getExpr();
        //XXX.XXX = ??
        if (expr instanceof LuaIndexExpr) {
            LuaIndexExpr indexExpr = (LuaIndexExpr) expr;
            return indexExpr.getId() != null;
        } else {
            //XXX = ??
            LuaNameExpr nameRef = (LuaNameExpr) expr;
            SearchContext context = new SearchContext(var.getProject());
            context.setCurrentStubFile(var.getContainingFile());
            return LuaPsiResolveUtil.resolveLocal(nameRef, context) == null;
        }
    }

    public String getTypeName() {
        if (typeName == null) {
            SearchContext context = new SearchContext(indexExpr.getProject());
            context.setCurrentStubFile(indexExpr.getContainingFile());

            LuaTypeSet set = indexExpr.guessPrefixType(context);
            if (set != null) {
                LuaType type = set.getPerfect();
                if (type != null)
                    typeName = type.getClassName();
            }
        }
        return typeName;
    }

    @Override
    public String getFieldName() {
        if (fieldName == null) {
            PsiElement id = indexExpr.getId();
            if (id != null)
                fieldName = id.getText();
        }
        return fieldName;
    }

    @Override
    public boolean isGlobal() {
        return isGlobal;
    }

}
