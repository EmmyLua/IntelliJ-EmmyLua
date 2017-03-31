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
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaCallExpr;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/12/14.
 */
public class LuaCallExprReference extends PsiReferenceBase<LuaCallExpr> {
    private LuaCallExpr expr;

    LuaCallExprReference(LuaCallExpr callExpr) {
        super(callExpr);
        expr = callExpr;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement newId = LuaElementFactory.createIdentifier(myElement.getProject(), newElementName);
        PsiElement oldId = expr.getExpr();
        assert oldId != null;
        oldId.replace(newId);
        return newId;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(element, resolve());
    }

    @Override
    public TextRange getRangeInElement() {
        PsiElement id = expr.getExpr();
        assert id != null;
        return new TextRange(id.getStartOffsetInParent(), id.getStartOffsetInParent() + id.getTextLength());
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        //TODO implement it
        /*PsiElement id = expr.getId();
        if (id != null) {
            SearchContext context = new SearchContext(myElement.getProject());
            LuaTypeSet typeSet = expr.guessPrefixType(context);
            if (typeSet != null) {
                String methodName = id.getText();
                for (LuaType luaType : typeSet.getTypes()) {
                    LuaClassMethodDef def = luaType.findMethod(methodName, context);
                    if (def != null)
                        return def;

                    //也有可能是字段
                    LuaClassField field = luaType.findField(methodName, context);
                    if (field != null)
                        return field;
                }
            }
        }*/
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
