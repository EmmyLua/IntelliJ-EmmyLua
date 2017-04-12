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

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.lang.type.LuaTableType;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaIndexStub;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2017/4/12.
 */
public class LuaIndexExpressionImpl extends StubBasedPsiElementBase<LuaIndexStub> implements LuaExpression, LuaClassField {

    public LuaIndexExpressionImpl(@NotNull LuaIndexStub stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public LuaIndexExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    public LuaIndexExpressionImpl(LuaIndexStub stub, IElementType nodeType, ASTNode node) {
        super(stub, nodeType, node);
    }


    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this, PsiReferenceService.Hints.NO_HINTS);
    }

    @Override
    public PsiReference getReference() {
        PsiReference[] references = getReferences();

        if (references.length > 0)
            return references[0];
        return null;
    }

    @Override
    public LuaTypeSet guessType(SearchContext context) {
        return guessType((LuaIndexExpr)this, context);
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

    @Override
    public String getFieldName() {
        LuaIndexStub stub = getStub();
        if (stub != null)
            return stub.getFieldName();
        return getName();
    }
}
