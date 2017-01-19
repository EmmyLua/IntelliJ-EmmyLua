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

package com.tang.intellij.lua.comment.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocParamDef;
import com.tang.intellij.lua.comment.psi.LuaDocParamNameRef;
import com.tang.intellij.lua.comment.psi.LuaDocTypeDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.psi.LuaTypes;
import com.tang.intellij.lua.search.SearchContext;

/**
 * Created by Tangzx on 2016/11/21.
 *
 * @qq 272669294
 */
public class LuaCommentImpl extends LazyParseablePsiElement implements LuaComment {

    public LuaCommentImpl(CharSequence charSequence) {
        super(LuaTypes.DOC_COMMENT, charSequence);
    }

    @Override
    public IElementType getTokenType() {
        return LuaTypes.DOC_COMMENT;
    }

    @Override
    public LuaCommentOwner getOwner() {
        return LuaCommentUtil.findOwner(this);
    }

    @Override
    public LuaDocParamDef getParamDef(String name) {
        PsiElement element = getFirstChild();
        while (element != null) {
            if (element instanceof LuaDocParamDef) {
                LuaDocParamDef paramDef = (LuaDocParamDef) element;
                LuaDocParamNameRef nameRef = paramDef.getParamNameRef();
                if (nameRef != null && nameRef.getText().equals(name))
                    return paramDef;
            }
            element = element.getNextSibling();
        }
        return null;
    }

    @Override
    public LuaDocClassDef getClassDef() {
        PsiElement element = getFirstChild();
        while (element != null) {
            if (element instanceof LuaDocClassDef) {
                return (LuaDocClassDef) element;
            }
            element = element.getNextSibling();
        }
        return null;
    }

    @Override
    public LuaDocTypeDef getTypeDef() {
        PsiElement element = getFirstChild();
        while (element != null) {
            if (element instanceof LuaDocTypeDef) {
                return (LuaDocTypeDef) element;
            }
            element = element.getNextSibling();
        }
        return null;
    }

    @Override
    public LuaTypeSet guessType(SearchContext context) {
        LuaDocClassDef classDef = getClassDef();
        if (classDef != null)
            return LuaTypeSet.create(classDef);
        LuaDocTypeDef typeDef = getTypeDef();
        if (typeDef != null)
            return typeDef.guessType(context);
        return null;
    }

    @Override
    public String toString() {
        return "DOC_COMMENT";
    }
}
