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
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaPsiResolveUtilKt;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/12/4.
 */
public class LuaIndexReference extends PsiReferenceBase<LuaIndexExpr> implements LuaReference {

    private PsiElement id;

    LuaIndexReference(@NotNull LuaIndexExpr element, PsiElement id) {
        super(element);
        this.id = id;
    }

    @Override
    public TextRange getRangeInElement() {
        int start = id.getNode().getStartOffset() - myElement.getNode().getStartOffset();
        return new TextRange(start, start + id.getTextLength());
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement newId = LuaElementFactory.createIdentifier(myElement.getProject(), newElementName);
        id.replace(newId);
        return newId;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(resolve(), element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return resolve(new SearchContext(myElement.getProject()));
    }

    @Override
    public PsiElement resolve(SearchContext context) {
        PsiElement ref = LuaPsiResolveUtilKt.resolve(myElement, context);
        if (ref != null) {
            if (ref.getContainingFile().equals(myElement.getContainingFile())) { //优化，不要去解析 Node Tree
                if (ref.getNode().getTextRange().equals(myElement.getNode().getTextRange())) {
                    return null;//自己引用自己
                }
            }
        }
        return ref;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
