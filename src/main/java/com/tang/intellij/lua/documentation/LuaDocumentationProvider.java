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

package com.tang.intellij.lua.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Documentation support
 * Created by tangzx on 2016/12/10.
 */
public class LuaDocumentationProvider extends AbstractDocumentationProvider implements DocumentationProvider {

    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof LuaCommentOwner) {
            return genDocForCommentOwner((LuaCommentOwner) element);
        }
        return super.generateDoc(element, originalElement);
    }

    private String genDocForCommentOwner(LuaCommentOwner owner) {
        StringBuilder sb = new StringBuilder();
        if (owner instanceof LuaClassMethodDef) {
            LuaClassMethodDef methodDef = (LuaClassMethodDef) owner;
            sb.append("<h1>");
            sb.append(methodDef.getName());
            sb.append("()</h1><br>");
        } else if (owner instanceof LuaGlobalFuncDef) {
            LuaGlobalFuncDef globalFuncDef = (LuaGlobalFuncDef) owner;
            sb.append("<h1>");
            sb.append(globalFuncDef.getName());
            sb.append("()</h1><br>");
        }

        LuaComment comment = LuaCommentUtil.findComment(owner);
        if (comment != null) {
            PsiElement child = comment.getFirstChild();
            while (child != null) {
                IElementType elementType = child.getNode().getElementType();
                if (elementType == LuaDocTypes.STRING) {
                    sb.append(child.getText());
                    sb.append("<br>");
                } else if (elementType == LuaDocTypes.PARAM_DEF) {
                    LuaDocParamDef paramDef = (LuaDocParamDef) child;
                    LuaDocParamNameRef paramNameRef = paramDef.getParamNameRef();
                    if (paramNameRef != null) {
                        sb.append("<li><b>param</b> ");
                        sb.append(paramNameRef.getText());
                        sb.append(" ");
                        getTypeSet(paramDef.getTypeSet(), sb);
                        sb.append("<br>");
                    }
                } else if (elementType == LuaDocTypes.RETURN_DEF) {
                    LuaDocReturnDef returnDef = (LuaDocReturnDef) child;
                    LuaDocTypeList typeList = returnDef.getTypeList();
                    if (typeList != null) {
                        sb.append("<li><b>return</b> ");
                        List<LuaDocTypeSet> typeSetList = typeList.getTypeSetList();
                        for (LuaDocTypeSet typeSet : typeSetList) {
                            getTypeSet(typeSet, sb);
                            sb.append(" ");
                        }
                        sb.append("<br>");
                    }
                }
                child = child.getNextSibling();
            }
        }

        String doc = sb.toString();
        if (doc.length() > 0)
            return sb.toString();
        else
            return null;
    }

    private void getTypeSet(LuaDocTypeSet typeSet, StringBuilder sb) {
        if (typeSet != null) {
            sb.append("(");
            List<LuaDocClassNameRef> nameRefList = typeSet.getClassNameRefList();
            String[] names = new String[nameRefList.size()];
            for (int i = 0; i < nameRefList.size(); i++) {
                names[i] = nameRefList.get(i).getText();
            }
            sb.append(String.join(", ", names));
            sb.append(")");
        }
    }
}
