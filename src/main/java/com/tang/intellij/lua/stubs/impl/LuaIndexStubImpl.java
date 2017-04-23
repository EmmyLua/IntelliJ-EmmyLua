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
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaIndexStub;
import com.tang.intellij.lua.stubs.types.LuaIndexType;

import java.util.Optional;

/**
 *
 * Created by TangZX on 2017/4/12.
 */
public class LuaIndexStubImpl extends StubBase<LuaIndexExpr> implements LuaIndexStub {
    private LuaIndexExpr indexExpr;
    private String typeName;
    private String fieldName;
    private LuaTypeSet valueType;

    public LuaIndexStubImpl(LuaIndexExpr indexExpr, StubElement parent, LuaIndexType elementType) {
        super(parent, elementType);
        this.indexExpr = indexExpr;

        fieldName = indexExpr.getName();
    }

    public LuaIndexStubImpl(String typeName, String fieldName, LuaTypeSet valueType, StubElement stubElement, LuaIndexType indexType) {
        super(stubElement, indexType);
        this.typeName = typeName;
        this.fieldName = fieldName;
        this.valueType = valueType;
    }

    @Override
    public String getTypeName() {
        if (typeName == null && indexExpr != null) {
            SearchContext context = new SearchContext(indexExpr.getProject());
            context.setCurrentStubFile(indexExpr.getContainingFile());
            LuaTypeSet typeSet = indexExpr.guessPrefixType(context);
            if (typeSet != null) {
                LuaType type = typeSet.getPerfect();
                if (type != null)
                    typeName = type.getClassName();
            }
        }
        return typeName;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public LuaTypeSet guessValueType() {
        if (PowerLevel.isFullPower() && valueType == null && indexExpr != null) {
            Optional<LuaTypeSet> setOptional = Optional.of(indexExpr)
                    .filter(s -> s.getParent() instanceof LuaVar)
                    .map(PsiElement::getParent)
                    .filter(s -> s.getParent() instanceof LuaVarList)
                    .map(PsiElement::getParent)
                    .filter(s -> s.getParent() instanceof LuaAssignStat)
                    .map(PsiElement::getParent)
                    .map(s -> {
                        LuaAssignStat assignStat = (LuaAssignStat) s;
                        LuaExprList exprList = assignStat.getExprList();
                        if (exprList != null) {
                            return exprList.guessTypeAt(0, new SearchContext(indexExpr.getProject()));
                        }
                        return null;
                    });
            valueType = setOptional.orElse(null);
        }
        return valueType;
    }
}