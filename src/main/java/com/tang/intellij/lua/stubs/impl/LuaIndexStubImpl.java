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
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.PowerLevel;
import com.tang.intellij.lua.psi.LuaAssignStat;
import com.tang.intellij.lua.psi.LuaExprList;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaVarList;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaIndexStub;
import com.tang.intellij.lua.stubs.types.LuaIndexType;
import com.tang.intellij.lua.ty.TyClass;
import com.tang.intellij.lua.ty.TySet;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 *
 * Created by TangZX on 2017/4/12.
 */
public class LuaIndexStubImpl extends StubBase<LuaIndexExpr> implements LuaIndexStub {
    private LuaIndexExpr indexExpr;
    private String typeName;
    private String fieldName;
    private TySet valueType = TySet.Companion.getEMPTY();

    public LuaIndexStubImpl(LuaIndexExpr indexExpr, StubElement parent, LuaIndexType elementType) {
        super(parent, elementType);
        this.indexExpr = indexExpr;

        fieldName = indexExpr.getName();
    }

    public LuaIndexStubImpl(String typeName, String fieldName, TySet valueType, StubElement stubElement, LuaIndexType indexType) {
        super(stubElement, indexType);
        this.typeName = typeName;
        this.fieldName = fieldName;
        if (valueType != null)
            this.valueType = valueType;
    }

    @Override
    public String getTypeName() {
        if (typeName == null && indexExpr != null) {
            SearchContext context = new SearchContext(indexExpr.getProject());
            context.setCurrentStubFile(indexExpr.getContainingFile());
            TySet typeSet = indexExpr.guessPrefixType(context);
            TyClass type = typeSet.getPerfectClass();
            if (type != null)
                typeName = type.getClassName();
        }
        return typeName;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @NotNull
    @Override
    public TySet guessValueType() {
        if (PowerLevel.isFullPower() && valueType == null && indexExpr != null) {
            Optional<TySet> setOptional = Optional.of(indexExpr)
                    .filter(s -> s.getParent() instanceof LuaVarList)
                    .map(PsiElement::getParent)
                    .filter(s -> s.getParent() instanceof LuaAssignStat)
                    .map(PsiElement::getParent)
                    .map(s -> {
                        LuaAssignStat assignStat = (LuaAssignStat) s;
                        LuaExprList exprList = assignStat.getValueExprList();
                        if (exprList != null) {
                            //todo set index
                            return exprList.guessTypeAt(new SearchContext(indexExpr.getProject()));
                        }
                        return null;
                    });
            valueType = setOptional.orElse(TySet.Companion.getEMPTY());
        }
        return valueType;
    }
}