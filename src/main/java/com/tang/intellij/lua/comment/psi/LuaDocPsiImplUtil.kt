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

@file:Suppress("UNUSED_PARAMETER")

package com.tang.intellij.lua.comment.psi

import com.intellij.icons.AllIcons
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.reference.LuaClassNameReference
import com.tang.intellij.lua.comment.reference.LuaDocParamNameReference
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.psi.LuaElementFactory
import com.tang.intellij.lua.psi.Visibility
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*
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

fun resolveType(nameRef: LuaDocClassNameRef): ITy {
    return when (nameRef.text){
        Constants.WORD_NIL -> Ty.NIL
        Constants.WORD_ANY -> Ty.UNKNOWN
        Constants.WORD_BOOLEAN -> Ty.BOOLEAN
        Constants.WORD_STRING -> Ty.STRING
        Constants.WORD_NUMBER -> Ty.NUMBER
        Constants.WORD_TABLE -> Ty.TABLE
        Constants.WORD_FUNCTION -> Ty.FUNCTION
        else -> TyLazyClass(nameRef.text)
    }
}

fun getName(identifierOwner: PsiNameIdentifierOwner): String? {
    val id = identifierOwner.nameIdentifier
    return id?.text
}

fun setName(identifierOwner: PsiNameIdentifierOwner, newName: String): PsiElement {
    val oldId = identifierOwner.nameIdentifier
    if (oldId != null) {
        val newId = LuaElementFactory.createDocIdentifier(identifierOwner.project, newName)
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

fun guessType(fieldDef: LuaDocFieldDef, context: SearchContext): ITy {
    val stub = fieldDef.stub
    if (stub != null)
        return stub.type
    return fieldDef.ty?.getType() ?: Ty.UNKNOWN
}

fun guessParentType(fieldDef: LuaDocFieldDef, context: SearchContext): ITy {
    val parent = fieldDef.parent
    val classDef = PsiTreeUtil.findChildOfType(parent, LuaDocClassDef::class.java)
    return classDef?.type ?: Ty.UNKNOWN
}

fun getVisibility(fieldDef: LuaDocFieldDef): Visibility {
    return Visibility.PUBLIC
}

/**
 * 猜测参数的类型
 * @param paramDec 参数定义
 * *
 * @return 类型集合
 */
fun getType(paramDec: LuaDocParamDef): ITy {
    return paramDec.ty?.getType() ?: return Ty.UNKNOWN
}

/**
 * 获取返回类型
 * @param returnDef 返回定义
 *
 * @return 类型集合
 */
fun resolveTypeAt(returnDef: LuaDocReturnDef, index: Int): ITy {
    val typeList = returnDef.typeList
    if (typeList != null) {
        val list = typeList.tyList
        if (list.size > index) {
            return list[index].getType()
        }
    }
    return Ty.UNKNOWN
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

fun getType(classDef: LuaDocClassDef): ITyClass {
    val stub = classDef.stub
    return stub?.classType ?: TyPsiDocClass(classDef)
}

/**
 * 猜测类型
 * @param typeDef 类型定义
 * *
 * @return 类型集合
 */
fun getType(typeDef: LuaDocTypeDef): ITy {
    return typeDef.ty?.getType() ?: Ty.UNKNOWN
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

fun getType(luaDocArrTy: LuaDocArrTy): ITy {
    val baseTy = luaDocArrTy.ty.getType()
    return TyArray(baseTy)
}

fun getType(luaDocGeneralTy: LuaDocGeneralTy): ITy {
    return resolveType(luaDocGeneralTy.classNameRef)
}

fun getType(luaDocFunctionTy: LuaDocFunctionTy): ITy {
    return TyDocPsiFunction(luaDocFunctionTy)
}

fun getReturnType(luaDocFunctionTy: LuaDocFunctionTy): ITy {
    val set = luaDocFunctionTy.typeList?.tyList?.firstOrNull()
    return set?.getType() ?: Ty.UNKNOWN
}

fun getType(luaDocGenericTy: LuaDocGenericTy): ITy {
    return TyDocGeneric(luaDocGenericTy)
}

fun getType(luaDocParTy: LuaDocParTy): ITy {
    return luaDocParTy.ty?.getType() ?: Ty.UNKNOWN
}

fun getType(unionTy: LuaDocUnionTy): ITy {
    val list = unionTy.tyList
    var retTy: ITy = Ty.UNKNOWN
    for (ty in list) {
        retTy = retTy.union(ty.getType())
    }
    return retTy
}