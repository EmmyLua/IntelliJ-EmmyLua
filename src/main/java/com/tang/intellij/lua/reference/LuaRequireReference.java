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
import com.tang.intellij.lua.lang.type.LuaString;
import com.tang.intellij.lua.psi.LuaCallExpr;
import com.tang.intellij.lua.psi.LuaPsiResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/12/9.
 */
public class LuaRequireReference extends PsiReferenceBase<LuaCallExpr> {

    private String pathString;
    private TextRange range = TextRange.EMPTY_RANGE;

    LuaRequireReference(@NotNull LuaCallExpr callExpr) {
        super(callExpr);

        PsiElement path = callExpr.getFirstStringArg();

        if (path != null && path.getTextLength() > 2) {
            LuaString luaString = LuaString.getContent(path.getText());
            pathString = luaString.value;

            int start = path.getTextOffset() - callExpr.getTextOffset() + luaString.start;
            int end = start + pathString.length();
            range = new TextRange(start, end);
        }
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(element, resolve());
    }

    @Override
    public TextRange getRangeInElement() {
        return range;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return LuaPsiResolveUtil.resolveRequireFile(pathString, myElement.getProject());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
