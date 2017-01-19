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

package com.tang.intellij.lua.comment.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocClassNameRef;
import com.tang.intellij.lua.stubs.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 *
 * Created by TangZX on 2016/11/29.
 */
public class LuaClassNameReference extends PsiReferenceBase<LuaDocClassNameRef> {
    public LuaClassNameReference(@NotNull LuaDocClassNameRef element) {
        super(element);
    }

    @Override
    public TextRange getRangeInElement() {
        return new TextRange(0, myElement.getTextLength());
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(element, resolve());
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        String name = myElement.getText();
        Collection<LuaDocClassDef> defs = LuaClassIndex.getInstance().get(name, myElement.getProject(), new ProjectAndLibrariesScope(myElement.getProject()));
        if (defs.size() > 0) {
            LuaDocClassDef def = defs.iterator().next();
            return def;
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
