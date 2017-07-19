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

package com.tang.intellij.lua.comment.psi

import com.intellij.icons.AllIcons
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement
import com.tang.intellij.lua.comment.reference.LuaClassNameReference
import com.tang.intellij.lua.comment.reference.LuaDocParamNameReference
import com.tang.intellij.lua.lang.type.LuaType
import com.tang.intellij.lua.lang.type.LuaTypeSet
import com.tang.intellij.lua.psi.LuaElementFactory
import com.tang.intellij.lua.psi.aliasName
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassIndex
import javax.swing.Icon

/**

 * Created by TangZX on 2016/11/24.
 */
fun getReference(paramNameRef: LuaDocParamNameRef): PsiReference {
    return LuaDocParamNameReference(paramNameRef)
}

fun getReference(docClassNameRef: LuaDocClassNameRef): PsiReference {
    return LuaClassNameReference(docClassNameRef)
}

fun resolveType(nameRef: LuaDocClassNameRef, context: SearchContext): LuaType? {
    val classDef = LuaClassIndex.find(nameRef.text, context)
    if (classDef != null) {
        return classDef.classType
    }
    return null
}

fun getName(identifierOwner: PsiNameIdentifierOwner): String? {
    val id = identifierOwner.nameIdentifier
    return id?.text
}

fun setName(identifierOwner: PsiNameIdentifierOwner, newName: String): PsiElement {
    val oldId = identifierOwner.nameIdentifier
    if (oldId != null) {
        val newId = LuaElementFactory.createIdentifier(identifierOwner.project, newName)
        oldId.replace(newId)
        return newId
    }
    return identifierOwner
}

fun getTextOffset(identifierOwner: PsiNameIdentifierOwner): Int {
    val id = identifierOwner.nameIdentifier
    return id?.textOffset ?: identifierOwner.node.startOffset
}

fun getNameIdentifier(fieldDef: LuaDocFieldDef): PsiElement? {
    return fieldDef.id
}

fun getNameIdentifier(classDef: LuaDocClassDef): PsiElement {
    return classDef.id
}

fun guessType(fieldDef: LuaDocFieldDef, context: SearchContext): LuaTypeSet? {
    val stub = fieldDef.stub
    if (stub != null)
        return stub.type
    return resolveDocTypeSet(fieldDef.typeSet, null, context)
}

/**
 * 猜测参数的类型
 * @param paramDec 参数定义
 * *
 * @return 类型集合
 */
fun guessType(paramDec: LuaDocParamDef, context: SearchContext): LuaTypeSet? {
    val docTypeSet = paramDec.typeSet ?: return null
    return resolveDocTypeSet(docTypeSet, null, context)
}

/**
 * 获取返回类型
 * @param returnDef 返回定义
 * *
 * @param index 索引
 * *
 * @return 类型集合
 */
fun resolveTypeAt(returnDef: LuaDocReturnDef, index: Int, context: SearchContext): LuaTypeSet {
    val typeSet = LuaTypeSet.create()
    val typeList = returnDef.typeList
    if (typeList != null) {
        val typeSetList = typeList.typeSetList
        val docTypeSet = typeSetList[index]
        resolveDocTypeSet(docTypeSet, typeSet, context)
    }
    return typeSet
}

fun resolveDocTypeSet(docTypeSet: LuaDocTypeSet?, set: LuaTypeSet?, context: SearchContext): LuaTypeSet? {
    var typeSet = set
    if (typeSet == null) typeSet = LuaTypeSet.create()
    if (docTypeSet != null) {
        val classNameRefList = docTypeSet.classNameRefList
        for (classNameRef in classNameRefList) {
            val def = LuaClassIndex.find(classNameRef.text, context)
            if (def != null) {
                typeSet = LuaTypeSet.union(typeSet, def.classType)
            } else {
                typeSet = LuaTypeSet.union(typeSet, LuaType.create(classNameRef.text, null))
            }
        }
    }
    return typeSet
}

/**
 * 优化：从stub中取名字
 * @param classDef LuaDocClassDef
 * *
 * @return string
 */
fun getName(classDef: LuaDocClassDef): String {
    val stub = classDef.stub
    if (stub != null)
        return stub.className
    return classDef.id.text
}

/**
 * for Goto Class
 * @param classDef class def
 * *
 * @return ItemPresentation
 */
fun getPresentation(classDef: LuaDocClassDef): ItemPresentation {
    return object : ItemPresentation {
        override fun getPresentableText(): String? {
            return classDef.name
        }

        override fun getLocationString(): String? {
            return classDef.containingFile.name
        }

        override fun getIcon(b: Boolean): Icon? {
            return AllIcons.Nodes.Class
        }
    }
}

fun getClassType(classDef: LuaDocClassDef): LuaType {
    val stub = classDef.stub
    val luaType: LuaType
    if (stub != null) {
        luaType = stub.classType
    } else {
        val clazzName = classDef.name
        var superClassName: String? = null
        val supperRef = classDef.superClassNameRef
        if (supperRef != null)
            superClassName = supperRef.text

        luaType = LuaType.create(clazzName, superClassName)
        luaType.aliasName = classDef.aliasName
    }
    return luaType
}

/**
 * 猜测类型
 * @param typeDef 类型定义
 * *
 * @return 类型集合
 */
fun guessType(typeDef: LuaDocTypeDef, context: SearchContext): LuaTypeSet? {
    return resolveDocTypeSet(typeDef.typeSet, null, context)
}

@Suppress("UNUSED_PARAMETER")
fun toString(stubElement: StubBasedPsiElement<out StubElement<*>>): String {
    return "[STUB]"// + stubElement.getNode().getElementType().toString();
}

fun getName(fieldDef: LuaDocFieldDef): String? {
    val stub = fieldDef.stub
    if (stub != null)
        return stub.name
    return getName(fieldDef as PsiNameIdentifierOwner)
}

fun getFieldName(fieldDef: LuaDocFieldDef): String? {
    val stub = fieldDef.stub
    if (stub != null)
        return stub.name
    return fieldDef.name
}

fun getPresentation(fieldDef: LuaDocFieldDef): ItemPresentation {
    return object : ItemPresentation {
        override fun getPresentableText(): String? {
            return fieldDef.name
        }

        override fun getLocationString(): String? {
            return fieldDef.containingFile.name
        }

        override fun getIcon(b: Boolean): Icon? {
            return AllIcons.Nodes.Field
        }
    }
}