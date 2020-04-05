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
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.reference.LuaClassNameReference
import com.tang.intellij.lua.comment.reference.LuaDocParamNameReference
import com.tang.intellij.lua.comment.reference.LuaDocSeeReference
import com.tang.intellij.lua.lang.type.LuaNumber
import com.tang.intellij.lua.lang.type.LuaString
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.psi.LuaElementFactory
import com.tang.intellij.lua.psi.LuaPsiTreeUtil
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
    if (nameRef.id.text == Constants.WORD_SELF) {
        val contextClass = LuaPsiTreeUtil.findContextClass(nameRef) as? ITyClass
        return if (contextClass != null) TyClass.createSelfType(contextClass) else Ty.UNKNOWN
    }

    return LuaPsiTreeUtil.findGenericDef(nameRef.text, nameRef)?.type ?: Ty.create(nameRef.text)
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

fun getNameIdentifier(tagField: LuaDocTagField): PsiElement? {
    return tagField.id
}

fun getNameIdentifier(tagClass: LuaDocTagClass): PsiElement {
    return tagClass.id
}

fun getIndexType(tagField: LuaDocTagField): LuaDocTy? {
    return if (tagField.lbrack != null) tagField.tyList.firstOrNull() else null
}

fun getValueType(tagField: LuaDocTagField): LuaDocTy? {
    return tagField.tyList.getOrNull(if (tagField.lbrack != null) 1 else 0)
}

fun guessType(tagField: LuaDocTagField, context: SearchContext): ITy {
    val stub = tagField.stub
    if (stub != null)
        return stub.valueTy
    return tagField.valueType?.getType() ?: Ty.UNKNOWN
}

fun guessParentType(tagField: LuaDocTagField, context: SearchContext): ITy {
    val parent = tagField.parent
    val classDef = PsiTreeUtil.findChildOfType(parent, LuaDocTagClass::class.java)
    return classDef?.type ?: Ty.UNKNOWN
}

fun getVisibility(tagField: LuaDocTagField): Visibility {
    val stub = tagField.stub
    if (stub != null)
        return stub.visibility

    val v = tagField.accessModifier?.let { Visibility.get(it.text) }
    return v ?: Visibility.PUBLIC
}

/**
 * 猜测参数的类型
 * @param tagParamDec 参数定义
 * *
 * @return 类型集合
 */
fun getType(tagParamDec: LuaDocTagParam): ITy {
    val type = tagParamDec.ty?.getType()
    if (type != null) {
        val substitutor = LuaCommentUtil.findContainer(tagParamDec).createSubstitutor()
        if (substitutor != null)
            return type.substitute(substitutor)
    }
    return type ?: Ty.UNKNOWN
}

fun getType(vararg: LuaDocTagVararg): ITy {
    return vararg.ty?.getType() ?: Ty.UNKNOWN
}

fun getType(vararg: LuaDocVarargParam): ITy {
    return vararg.ty?.getType() ?: Ty.UNKNOWN
}

/**
 * 获取返回类型
 * @param tagReturn 返回定义
 *
 * @return 类型集合
 */
fun resolveTypeAt(tagReturn: LuaDocTagReturn, index: Int): ITy {
    val typeList = tagReturn.typeList
    if (typeList != null) {
        val list = typeList.tyList
        if (list.size > index) {
            return list[index].getType()
        }
    }
    return Ty.UNKNOWN
}

fun getType(tagReturn: LuaDocTagReturn): ITy {
    val tyList = tagReturn.typeList?.tyList
    val variadic = tagReturn.varreturn != null
    if (tyList != null && tyList.isNotEmpty()) {
        val tupleList = tyList.map { it.getType() }
        return if (tupleList.size == 1 && !variadic) tupleList.first() else TyMultipleResults(tupleList, variadic)
    }
    return Ty.VOID
}

/**
 * 优化：从stub中取名字
 * @param tagClass LuaDocClassDef
 * *
 * @return string
 */
fun getName(tagClass: LuaDocTagClass): String {
    val stub = tagClass.stub
    if (stub != null)
        return stub.className
    return tagClass.id.text
}

fun getName(tagAlias: LuaDocTagAlias): String {
    val stub = tagAlias.stub
    if (stub != null)
        return stub.name
    return tagAlias.id.text
}

/**
 * for Goto Class
 * @param tagClass class def
 * *
 * @return ItemPresentation
 */
fun getPresentation(tagClass: LuaDocTagClass): ItemPresentation {
    return object : ItemPresentation {
        override fun getPresentableText(): String? {
            return tagClass.name
        }

        override fun getLocationString(): String? {
            return tagClass.containingFile.name
        }

        override fun getIcon(b: Boolean): Icon? {
            return AllIcons.Nodes.Class
        }
    }
}

fun getType(tagClass: LuaDocTagClass): ITyClass {
    val stub = tagClass.stub
    return stub?.classType ?: TyPsiDocClass(tagClass)
}

fun getType(genericDef: LuaDocGenericDef): ITyClass {
    return TyParameter(genericDef)
}

fun isDeprecated(tagClass: LuaDocTagClass): Boolean {
    val stub = tagClass.stub
    return stub?.isDeprecated ?: LuaCommentUtil.findContainer(tagClass).isDeprecated
}

fun isShape(tagClass: LuaDocTagClass): Boolean {
    val stub = tagClass.stub
    return stub?.isShape ?: tagClass.shape != null
}

/**
 * 猜测类型
 * @param tagType 类型定义
 * *
 * @return 类型集合
 */
fun getType(tagType: LuaDocTagType): ITy {
    return tagType.ty?.getType() ?: Ty.UNKNOWN
}

@Suppress("UNUSED_PARAMETER")
fun toString(stubElement: StubBasedPsiElement<out StubElement<*>>): String {
    return "[STUB]"// + stubElement.getNode().getElementType().toString();
}

fun getName(tagField: LuaDocTagField): String? {
    val stub = tagField.stub
    if (stub != null)
        return stub.name
    return getName(tagField as PsiNameIdentifierOwner)
}

fun getFieldName(tagField: LuaDocTagField): String? {
    val stub = tagField.stub
    if (stub != null)
        return stub.name
    return tagField.name
}

fun getPresentation(tagField: LuaDocTagField): ItemPresentation {
    return object : ItemPresentation {
        override fun getPresentableText(): String? {
            return tagField.name
        }

        override fun getLocationString(): String? {
            return tagField.containingFile.name
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
    val list = luaDocFunctionTy.typeList?.tyList?.map { it.getType() }
    val variadic = luaDocFunctionTy.varreturn != null

    return when {
        list == null -> Ty.VOID
        list.isEmpty() -> Ty.VOID
        list.size == 1 && !variadic -> list.first()
        else -> TyMultipleResults(list, variadic)
    }
}

fun getType(luaDocGenericTy: LuaDocGenericTy): ITy {
    return TyDocGeneric(luaDocGenericTy)
}

fun getType(luaDocParTy: LuaDocParTy): ITy {
    return luaDocParTy.ty?.getType() ?: Ty.UNKNOWN
}

fun getType(booleanLiteral: LuaDocBooleanLiteralTy): ITy {
    return TyPrimitiveLiteral.getTy(TyPrimitiveKind.Boolean, booleanLiteral.value.text)
}

fun getType(numberLiteral: LuaDocNumberLiteralTy): ITy {
    val n = LuaNumber.getValue(numberLiteral.value.text)
    return if (n != null) TyPrimitiveLiteral.getTy(TyPrimitiveKind.Number, n.toString()) else Ty.UNKNOWN
}

fun getType(stringLiteral: LuaDocStringLiteralTy): ITy {
    return TyPrimitiveLiteral.getTy(TyPrimitiveKind.String, LuaString.getContent(stringLiteral.value.text).value)
}

fun getType(snippet: LuaDocSnippetTy): ITy {
    return TySnippet(snippet.content.text)
}

fun getType(unionTy: LuaDocUnionTy): ITy {
    val list = unionTy.tyList
    var retTy: ITy = Ty.VOID
    for (ty in list) {
        retTy = retTy.union(ty.getType())
    }
    return if (Ty.isInvalid(retTy)) Ty.UNKNOWN else retTy
}

fun getReference(see: LuaDocTagSee): PsiReference? {
    if (see.id == null) return null
    return LuaDocSeeReference(see)
}

fun getType(tbl: LuaDocTableTy): ITy {
    return TyDocTable(tbl.tableDef)
}

fun guessParentType(f: LuaDocTableField, context: SearchContext): ITy {
    val p = f.parent as LuaDocTableDef
    return TyDocTable(p)
}

fun getVisibility(f: LuaDocTableField): Visibility {
    return Visibility.PUBLIC
}

fun getNameIdentifier(f: LuaDocTableField): PsiElement? {
    return f.id
}

fun getName(f:LuaDocTableField): String? {
    val stub = f.stub
    if (stub != null)
        return stub.name
    return getName(f as PsiNameIdentifierOwner)
}

fun getIndexType(f: LuaDocTableField): LuaDocTy? {
    return if (f.lbrack != null) f.tyList.firstOrNull() else null
}

fun getValueType(f: LuaDocTableField): LuaDocTy? {
    return f.tyList.getOrNull(if (f.lbrack != null) 1 else 0)
}

fun guessType(f:LuaDocTableField, context: SearchContext): ITy {
    val stub = f.stub
    if (stub != null)
        return stub.valueTy ?: Ty.UNKNOWN
    return f.valueType?.getType() ?: Ty.UNKNOWN
}

fun getNameIdentifier(g: LuaDocGenericDef): PsiElement? {
    return g.id
}

fun isDeprecated(member: LuaClassMember): Boolean {
    return false
}

fun getNameIdentifier(g: LuaDocTagAlias): PsiElement? {
    return g.id
}

fun getType(alias: LuaDocTagAlias): ITy {
    val stub = alias.stub
    return stub?.type ?: TyAlias(alias.name, alias.genericDefList.map { TyParameter(it) }.toTypedArray(), alias.ty?.getType() ?: Ty.UNKNOWN)
}
