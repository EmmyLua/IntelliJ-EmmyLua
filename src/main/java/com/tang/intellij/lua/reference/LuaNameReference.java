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

package com.tang.intellij.lua.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.psi.LuaNameExpr;
import com.tang.intellij.lua.psi.LuaPsiResolveUtilKt;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/11/26.
 */
public class LuaNameReference extends PsiReferenceBase<LuaNameExpr> implements LuaReference {
    private PsiElement id;

    LuaNameReference(LuaNameExpr element) {
        super(element);
        id = element.getId();
    }

    @Override
    public TextRange getRangeInElement() {
        int start = id.getTextOffset() - myElement.getTextOffset();
        return new TextRange(start, start + id.getTextLength());
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement newId = LuaElementFactory.createIdentifier(myElement.getProject(), newElementName);
        id.replace(newId);
        return newId;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return resolve(new SearchContext(myElement.getProject()));
    }

    @Override
    public PsiElement resolve(SearchContext context) {
        PsiElement resolve = LuaPsiResolveUtilKt.resolve(myElement, context);
        if (resolve == myElement) // LuaVar 全局定义中会发生
            return null;
        return resolve;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(element, resolve());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
