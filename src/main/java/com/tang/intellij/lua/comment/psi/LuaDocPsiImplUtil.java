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

package com.tang.intellij.lua.comment.psi;

import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.comment.reference.LuaClassNameReference;
import com.tang.intellij.lua.comment.reference.LuaDocParamNameReference;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaDocClassFieldStub;
import com.tang.intellij.lua.stubs.LuaDocClassStub;
import com.tang.intellij.lua.stubs.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaDocPsiImplUtil {

    @NotNull
    public static PsiReference getReference(LuaDocParamNameRef paramNameRef) {
        return new LuaDocParamNameReference(paramNameRef);
    }

    @NotNull
    public static PsiReference getReference(LuaDocClassNameRef docClassNameRef) {
        return new LuaClassNameReference(docClassNameRef);
    }

    public static LuaType resolveType(LuaDocClassNameRef nameRef, SearchContext context) {
        LuaDocClassDef classDef = LuaClassIndex.find(nameRef.getText(), context);
        if (classDef != null) {
            return classDef.getClassType();
        }
        return null;
    }

    public static String getName(PsiNameIdentifierOwner identifierOwner) {
        PsiElement id = identifierOwner.getNameIdentifier();
        return id != null ? id.getText() : null;
    }

    public static PsiElement setName(PsiNameIdentifierOwner identifierOwner, String newName) {
        PsiElement oldId = identifierOwner.getNameIdentifier();
        if (oldId != null) {
            PsiElement newId = LuaElementFactory.createIdentifier(identifierOwner.getProject(), newName);
            oldId.replace(newId);
            return newId;
        }
        return identifierOwner;
    }

    public static int getTextOffset(PsiNameIdentifierOwner identifierOwner) {
        PsiElement id = identifierOwner.getNameIdentifier();
        return id != null ? id.getTextOffset() : identifierOwner.getNode().getStartOffset();
    }

    public static PsiElement getNameIdentifier(LuaDocFieldDef fieldDef) {
        return fieldDef.getId();
    }

    public static PsiElement getNameIdentifier(LuaDocClassDef classDef) {
        return classDef.getId();
    }

    public static LuaTypeSet guessType(LuaDocFieldDef fieldDef, SearchContext context) {
        LuaDocClassFieldStub stub = fieldDef.getStub();
        if (stub != null)
            return stub.getType();
        return resolveDocTypeSet(fieldDef.getTypeSet(), null, context);
    }

    /**
     * 猜测全局定义的类型
     * @param docGlobalDef 全局定义
     * @return 类型集合
     */
    public static LuaTypeSet guessType(LuaDocGlobalDef docGlobalDef, SearchContext context) {
        LuaComment comment = LuaCommentUtil.findContainer(docGlobalDef);
        LuaDocTypeDef docTypeDef = comment.getTypeDef();
        if (docTypeDef != null) {
            return resolveDocTypeSet(docTypeDef.getTypeSet(), null, context);
        }
        return null;
    }

    /**
     * 猜测参数的类型
     * @param paramDec 参数定义
     * @return 类型集合
     */
    public static LuaTypeSet guessType(LuaDocParamDef paramDec, SearchContext context) {
        LuaDocTypeSet docTypeSet = paramDec.getTypeSet();
        if (docTypeSet == null) return null;
        return resolveDocTypeSet(docTypeSet, null, context);
    }

    /**
     * 获取返回类型
     * @param returnDef 返回定义
     * @param index 索引
     * @return 类型集合
     */
    public static LuaTypeSet resolveTypeAt(LuaDocReturnDef returnDef, int index, SearchContext context) {
        LuaTypeSet typeSet = LuaTypeSet.create();
        LuaDocTypeList typeList = returnDef.getTypeList();
        if (typeList != null) {
            List<LuaDocTypeSet> typeSetList = typeList.getTypeSetList();
            LuaDocTypeSet docTypeSet = typeSetList.get(index);
            resolveDocTypeSet(docTypeSet, typeSet, context);
        }
        return typeSet;
    }

    public static LuaTypeSet resolveDocTypeSet(LuaDocTypeSet docTypeSet, LuaTypeSet typeSet, SearchContext context) {
        if (typeSet == null) typeSet = LuaTypeSet.create();
        if (docTypeSet != null) {
            List<LuaDocClassNameRef> classNameRefList = docTypeSet.getClassNameRefList();
            for (LuaDocClassNameRef classNameRef : classNameRefList) {
                LuaDocClassDef def = LuaClassIndex.find(classNameRef.getText(), context);
                if (def != null) {
                    typeSet.addType(def.getClassType());
                } else {
                    typeSet.addType(LuaType.create(classNameRef.getText(), null));
                }
            }
        }
        return typeSet;
    }

    /**
     * 优化：从stub中取名字
     * @param classDef LuaDocClassDef
     * @return string
     */
    public static String getName(LuaDocClassDef classDef) {
        LuaDocClassStub stub = classDef.getStub();
        if (stub != null)
            return stub.getClassName();
        return getName((PsiNameIdentifierOwner) classDef);
    }

    /**
     * for Goto Class
     * @param classDef class def
     * @return ItemPresentation
     */
    public static ItemPresentation getPresentation(LuaDocClassDef classDef) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return classDef.getName();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return classDef.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return AllIcons.Nodes.Class;
            }
        };
    }

    public static LuaType getClassType(LuaDocClassDef classDef) {
        LuaDocClassStub stub = classDef.getStub();
        if (stub != null)
            return stub.getClassType();

        String clazzName = classDef.getName();
        assert clazzName != null;
        String superClassName = null;
        LuaDocClassNameRef supperRef = classDef.getClassNameRef();
        if (supperRef != null)
            superClassName = supperRef.getText();

        return LuaType.create(clazzName, superClassName);
    }

    /**
     * 猜测类型
     * @param typeDef 类型定义
     * @return 类型集合
     */
    public static LuaTypeSet guessType(LuaDocTypeDef typeDef, SearchContext context) {
        return resolveDocTypeSet(typeDef.getTypeSet(), null, context);
    }

    public static String toString(StubBasedPsiElement<? extends StubElement> stubElement) {
        return "[STUB]";// + stubElement.getNode().getElementType().toString();
    }

    public static String getName(LuaDocFieldDef fieldDef) {
        LuaDocClassFieldStub stub = fieldDef.getStub();
        if (stub != null)
            return stub.getName();
        return getName((PsiNameIdentifierOwner)fieldDef);
    }

    public static String getFieldName(LuaDocFieldDef fieldDef) {
        LuaDocClassFieldStub stub = fieldDef.getStub();
        if (stub != null)
            return stub.getName();
        return fieldDef.getName();
    }
}
