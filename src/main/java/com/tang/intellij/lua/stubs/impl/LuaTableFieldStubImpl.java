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
import com.tang.intellij.lua.lang.type.LuaTableType;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.stubs.LuaTableFieldStub;

import java.util.Optional;

/**
 *
 * Created by tangzx on 2017/1/14.
 */
public class LuaTableFieldStubImpl extends StubBase<LuaTableField> implements LuaTableFieldStub {
    private LuaTableField tableField;
    private String typeName;
    private String fieldName;

    public LuaTableFieldStubImpl(LuaTableField field, StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
        tableField = field;
    }

    public LuaTableFieldStubImpl(String typeName, String fieldName, StubElement stubElement, IStubElementType elementType) {
        super(stubElement, elementType);
        this.typeName = typeName;
        this.fieldName = fieldName;
    }

    @Override
    public String getTypeName() {
        if (typeName == null && tableField != null) {
            LuaTableConstructor table = PsiTreeUtil.getParentOfType(tableField, LuaTableConstructor.class);
            Optional<String> optional = Optional.ofNullable(table)
                    .filter(s -> s.getParent() instanceof LuaValueExpr)
                    .map(PsiElement::getParent)
                    .filter(s -> s.getParent() instanceof LuaExprList)
                    .map(PsiElement::getParent)
                    .filter(s -> s.getParent() instanceof LuaAssignStat)
                    .map(PsiElement::getParent)
                    .map(s -> {
                        LuaAssignStat assignStat = (LuaAssignStat) s;
                        return LuaPsiImplUtil.getTypeName(assignStat, 0);
                    });
            typeName = optional.orElse(LuaTableType.getTypeName(table));
        }
        return typeName;
    }

    @Override
    public String getFieldName() {
        if (fieldName != null)
            return fieldName;
        if (tableField != null)
            return tableField.getFieldName();
        return null;
    }
}
