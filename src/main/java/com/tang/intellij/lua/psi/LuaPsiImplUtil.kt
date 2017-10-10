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

package com.tang.intellij.lua.psi

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.icons.AllIcons
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.RecursionManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocReturnDef
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.lang.type.LuaString
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaFuncBodyOwnerStub
import com.tang.intellij.lua.ty.*
import java.util.*
import javax.swing.Icon

fun setName(identifier: LuaNamedElement, name: String): PsiElement {
    val newId = LuaElementFactory.createIdentifier(identifier.project, name)
    val oldId = identifier.firstChild
    oldId.replace(newId)
    return newId
}

fun setName(owner: PsiNameIdentifierOwner, name: String): PsiElement {
    val oldId = owner.nameIdentifier
    if (oldId != null) {
        val newId = LuaElementFactory.createIdentifier(owner.project, name)
        oldId.replace(newId)
        return newId
    }
    return owner
}

fun getName(identifier: LuaNamedElement): String {
    return identifier.text
}

fun guessType(nameDef: LuaNameDef, context: SearchContext): ITy {
    return resolveType(nameDef, context)
}

fun getNameIdentifier(nameDef: LuaNameDef): PsiElement {
    return nameDef.firstChild
}

/**
 * LuaNameDef 只可能在本文件中搜
 * @param nameDef def
 * *
 * @return SearchScope
 */
fun getUseScope(nameDef: LuaNameDef): SearchScope {
    return GlobalSearchScope.fileScope(nameDef.containingFile)
}

fun getReferences(element: LuaPsiElement): Array<PsiReference> {
    return ReferenceProvidersRegistry.getReferencesFromProviders(element, PsiReferenceService.Hints.NO_HINTS)
}

/**
 * 寻找 Comment
 * @param declaration owner
 * *
 * @return LuaComment
 */
fun getComment(declaration: LuaCommentOwner): LuaComment? {
    return LuaCommentUtil.findComment(declaration)
}

fun getNameIdentifier(classMethodDef: LuaClassMethodDef): PsiElement {
    return classMethodDef.classMethodName.id
}

fun getName(classMethodDef: LuaClassMethodDef): String? {
    val stub = classMethodDef.stub
    if (stub != null)
        return stub.name
    return getName(classMethodDef as PsiNameIdentifierOwner)
}

fun isStatic(classMethodDef: LuaClassMethodDef): Boolean {
    val stub = classMethodDef.stub
    if (stub != null)
        return stub.isStatic

    return classMethodDef.classMethodName.dot != null
}

fun getPresentation(classMethodDef: LuaClassMethodDef): ItemPresentation {
    return object : ItemPresentation {
        override fun getPresentableText(): String? {
            val type = classMethodDef.guessClassType(SearchContext(classMethodDef.project))
            if (type != null) {
                val c = if (classMethodDef.isStatic) "." else ":"
                return type.displayName + c + classMethodDef.name + classMethodDef.paramSignature
            }
            return classMethodDef.name!! + classMethodDef.paramSignature
        }

        override fun getLocationString(): String {
            return classMethodDef.containingFile.name
        }

        override fun getIcon(b: Boolean): Icon? {
            return LuaIcons.CLASS_METHOD
        }
    }
}

private val GET_CLASS_METHOD = Key.create<ParameterizedCachedValue<ITyClass, SearchContext>>("GET_CLASS_METHOD")

/**
 * 寻找对应的类
 * @param classMethodDef def
 * *
 * @return LuaType
 */
fun guessParentType(classMethodDef: LuaClassMethodDef, context: SearchContext): ITy {
    return CachedValuesManager.getManager(classMethodDef.project).getParameterizedCachedValue(classMethodDef, GET_CLASS_METHOD, { ctx ->
        val stub = classMethodDef.stub
        var type: ITyClass? = null
        if (stub != null) {
            type = TySerializedClass(stub.className)
        } else {
            val expr = classMethodDef.classMethodName.expr
            val ty = expr.guessTypeFromCache(ctx)
            val perfect = TyUnion.getPrefectClass(ty)
            if (perfect is ITyClass)
                type = perfect
        }
        CachedValueProvider.Result.create(type, classMethodDef)
    }, false, context) ?: Ty.UNKNOWN
}

fun getNameIdentifier(funcDef: LuaFuncDef): PsiElement? {
    return funcDef.id
}

fun getName(funcDef: LuaFuncDef): String? {
    val stub = funcDef.stub
    if (stub != null)
        return stub.name
    return getName(funcDef as PsiNameIdentifierOwner)
}

fun getPresentation(funcDef: LuaFuncDef): ItemPresentation {
    return object : ItemPresentation {
        override fun getPresentableText(): String? {
            return funcDef.name!! + funcDef.paramSignature
        }

        override fun getLocationString(): String {
            return funcDef.containingFile.name
        }

        override fun getIcon(b: Boolean): Icon? {
            return AllIcons.Nodes.Function
        }
    }
}

fun guessParentType(funcDef: LuaFuncDef, searchContext: SearchContext): ITyClass {
    return TyClass.G
}

/**
 * 猜出前面的类型
 * @param callExpr call expr
 * *
 * @return LuaTypeSet
 */
fun guessParentType(callExpr: LuaCallExpr, context: SearchContext): ITy {
    val prefix = callExpr.firstChild as LuaExpr
    return prefix.guessTypeFromCache(context)
}

/**
 * 找出函数体
 * @param callExpr call expr
 * *
 * @return LuaFuncBodyOwner
 */
fun resolveFuncBodyOwner(callExpr: LuaCallExpr, context: SearchContext): LuaFuncBodyOwner? {
    return RecursionManager.doPreventingRecursion<LuaFuncBodyOwner>(callExpr, true) {
        var owner: LuaFuncBodyOwner? = null
        val expr = callExpr.expr
        if (expr is LuaIndexExpr) {
            val resolve = resolve(expr, context)
            if (resolve is LuaFuncBodyOwner)
                owner = resolve
        } else if (expr is LuaNameExpr) {
            owner = resolveFuncBodyOwner(expr, context)
        }
        owner
    }
}

/**
 * 获取第一个字符串参数
 * @param callExpr callExpr
 * *
 * @return String PsiElement
 */
fun getFirstStringArg(callExpr: LuaCallExpr): PsiElement? {
    val args = callExpr.args
    var path: PsiElement? = null

    when (args) {
        is LuaSingleArg -> path = args.string
        is LuaListArgs -> args.exprList.let { list ->
            if (list.isNotEmpty() && list[0] is LuaLiteralExpr) {
                val valueExpr = list[0] as LuaLiteralExpr
                val node = valueExpr.firstChild
                if (node.node.elementType === LuaTypes.STRING) {
                    path = node
                }
            }
        }
    }
    return path
}

fun isStaticMethodCall(callExpr: LuaCallExpr): Boolean {
    val expr = callExpr.expr
    if (expr is LuaNameExpr)
        return true
    return expr is LuaIndexExpr && expr.colon == null
}

fun isMethodCall(callExpr: LuaCallExpr): Boolean {
    val expr = callExpr.expr
    return expr is LuaIndexExpr && expr.colon != null
}

fun isFunctionCall(callExpr: LuaCallExpr): Boolean {
    return callExpr.expr is LuaNameExpr
}

fun guessTypeAt(list: LuaExprList, context: SearchContext): ITy {
    val exprList = list.exprList
    val index = context.index
    if (exprList.size > index) {
        context.index = 0
        return exprList[index].guessTypeFromCache(context)
    } else if (exprList.size == 1) {
        val exp0 = exprList[0]
        return exp0.guessTypeFromCache(context)
    }
    return Ty.UNKNOWN
}

fun guessParentType(indexExpr: LuaIndexExpr, context: SearchContext): ITy {
    val prefix = indexExpr.firstChild as LuaExpr
    return prefix.guessTypeFromCache(context)
}

fun getNameIdentifier(indexExpr: LuaIndexExpr): PsiElement? {
    val id = indexExpr.id
    if (id != null)
        return id
    return indexExpr.idExpr
}

fun getPresentation(indexExpr: LuaIndexExpr): ItemPresentation {
    return object : ItemPresentation {
        override fun getPresentableText(): String? {
            return indexExpr.name
        }

        override fun getLocationString(): String {
            return indexExpr.containingFile.name
        }

        override fun getIcon(b: Boolean): Icon? {
            return LuaIcons.CLASS_FIELD
        }
    }
}

/**
 * xx['id']
 */
fun getIdExpr(indexExpr: LuaIndexExpr): LuaLiteralExpr? {
    val bracket = indexExpr.lbrack
    if (bracket != null) {
        val nextLeaf = PsiTreeUtil.getNextSiblingOfType(bracket, LuaExpr::class.java)
        if (nextLeaf is LuaLiteralExpr && nextLeaf.kind == LuaLiteralKind.String)
            return nextLeaf
    }
    return null
}

fun getName(indexExpr: LuaIndexExpr): String? {
    val stub = indexExpr.stub
    if (stub != null)
        return stub.name

    // var.name
    val id = indexExpr.id
    if (id != null)
        return id.text

    // var['name']
    val idExpr = indexExpr.idExpr
    if (idExpr != null)
        return LuaString.getContent(idExpr.text).value

    return null
}

fun setName(indexExpr: LuaIndexExpr, name: String): PsiElement {
    if (indexExpr.id != null)
        return setName(indexExpr as PsiNameIdentifierOwner, name)
    val idExpr = indexExpr.idExpr
    if (idExpr != null) {
        val text = idExpr.text
        val content = LuaString.getContent(text)
        val newText = text.substring(0, content.start) + name + text.substring(content.end)
        val newId = LuaElementFactory.createLiteral(indexExpr.project, newText)
        return idExpr.replace(newId)
    }
    return indexExpr
}

fun guessValueType(indexExpr: LuaIndexExpr, context: SearchContext): ITy {
    val stub = indexExpr.stub
    if (stub != null) {
        return stub.valueType
    }

    val setOptional = Optional.of(indexExpr)
            .filter { s -> s.parent is LuaVarList }
            .map<PsiElement>({ it.parent })
            .filter { s -> s.parent is LuaAssignStat }
            .map<PsiElement>({ it.parent })
            .map<ITy> { s ->
                val assignStat = s as LuaAssignStat
                context.index = assignStat.getIndexFor(indexExpr)
                assignStat.valueExprList?.guessTypeAt(context)
            }
    return setOptional.orElse(Ty.UNKNOWN)
}

fun findField(table: LuaTableExpr, fieldName: String): LuaTableField? {
    return table.tableFieldList.firstOrNull { fieldName == it.name }
}

fun getParamNameDefList(funcBodyOwner: LuaFuncBodyOwner): List<LuaParamNameDef> {
    val funcBody = funcBodyOwner.funcBody
    return funcBody?.paramNameDefList ?: emptyList()
}

fun getParamNameDefList(forAStat: LuaForAStat): List<LuaParamNameDef> {
    val list = ArrayList<LuaParamNameDef>()
    list.add(forAStat.paramNameDef)
    return list
}

fun guessReturnTypeSet(owner: LuaFuncBodyOwner, searchContext: SearchContext): ITy {
    if (owner is StubBasedPsiElementBase<*>) {
        val stub = owner.stub
        if (stub is LuaFuncBodyOwnerStub<*>) {
            return stub.returnTypeSet
        }
    }

    return guessReturnTypeSetInner(owner, searchContext)
}

private val FUNCTION_RETURN_TYPESET = Key.create<ParameterizedCachedValue<ITy, SearchContext>>("lua.function.return_typeset")

private fun guessReturnTypeSetInner(owner: LuaFuncBodyOwner, searchContext: SearchContext): ITy {
    if (owner is LuaCommentOwner) {
        val comment = LuaCommentUtil.findComment(owner)
        if (comment != null) {
            val returnDef = PsiTreeUtil.findChildOfType(comment, LuaDocReturnDef::class.java)
            if (returnDef != null) {
                return returnDef.resolveTypeAt(searchContext.index)
            }
        }
    }

    //infer from return stat
    return CachedValuesManager.getManager(owner.project).getParameterizedCachedValue(owner, FUNCTION_RETURN_TYPESET, { ctx ->
        var typeSet: ITy = Ty.UNKNOWN
        owner.acceptChildren(object : LuaVisitor() {
            override fun visitReturnStat(o: LuaReturnStat) {
                val guessReturnTypeSet = guessReturnTypeSet(o, ctx.index, ctx)
                TyUnion.each(guessReturnTypeSet) {
                    /**
                     * 注意，不能排除anonymous
                     * local function test()
                     *      local v = xxx()
                     *      v.yyy = zzz
                     *      return v
                     * end
                     *
                     * local r = test()
                     *
                     * type of r is an anonymous ty
                     */
                    typeSet = typeSet.union(it)
                }
            }

            override fun visitFuncBodyOwner(o: LuaFuncBodyOwner) {
                // ignore sub function
            }

            override fun visitClosureExpr(o: LuaClosureExpr) {

            }

            override fun visitPsiElement(o: LuaPsiElement) {
                o.acceptChildren(this)
            }
        })
        CachedValueProvider.Result.create(typeSet, owner)
    }, false, searchContext)
}

fun getParams(owner: LuaFuncBodyOwner): Array<LuaParamInfo> {
    if (owner is StubBasedPsiElementBase<*>) {
        val stub = owner.stub
        if (stub is LuaFuncBodyOwnerStub<*>) {
            return stub.params
        }
    }
    return getParamsInner(owner)
}

private fun getParamsInner(funcBodyOwner: LuaFuncBodyOwner): Array<LuaParamInfo> {
    var comment: LuaComment? = null
    if (funcBodyOwner is LuaCommentOwner) {
        comment = LuaCommentUtil.findComment(funcBodyOwner)
    }

    val paramNameList = funcBodyOwner.paramNameDefList
    if (paramNameList != null) {
        val list = mutableListOf<LuaParamInfo>()
        for (i in paramNameList.indices) {
            val paramInfo = LuaParamInfo()
            val paramName = paramNameList[i].text
            paramInfo.name = paramName
            // param types
            if (comment != null) {
                val paramDef = comment.getParamDef(paramName)
                if (paramDef != null) {
                    paramInfo.ty = paramDef.type
                }
            }
            list.add(paramInfo)
        }
        //check varArgs
        funcBodyOwner.funcBody?.ellipsis?.let {
            val args = LuaParamInfo()
            args.name = "..."
            args.isVarArgs = true
            list.add(args)
        }
        return list.toTypedArray()
    }
    return emptyArray()
}

fun getParamSignature(funcBodyOwner: LuaFuncBodyOwner): String {
    val params = funcBodyOwner.params
    val list = arrayOfNulls<String>(params.size)
    for (i in params.indices) {
        val lpi = params[i]
        list[i] = lpi.name
    }
    return "(" + list.joinToString(", ") + ")"
}

fun getNameIdentifier(localFuncDef: LuaLocalFuncDef): PsiElement? {
    return localFuncDef.id
}

fun getUseScope(localFuncDef: LuaLocalFuncDef): SearchScope {
    return GlobalSearchScope.fileScope(localFuncDef.containingFile)
}

fun getName(identifierOwner: PsiNameIdentifierOwner): String? {
    val id = identifierOwner.nameIdentifier
    return id?.text
}

fun getTextOffset(localFuncDef: PsiNameIdentifierOwner): Int {
    val id = localFuncDef.nameIdentifier
    if (id != null) return id.textOffset
    return localFuncDef.node.startOffset
}

fun getNameIdentifier(tableField: LuaTableField): PsiElement? {
    val id = tableField.id
    if (id != null)
        return id
    return tableField.idExpr
}

fun guessParentType(tableField: LuaTableField, context: SearchContext): ITy {
    val tbl = PsiTreeUtil.getParentOfType(tableField, LuaTableExpr::class.java)!!
    return tbl.guessType(context)
}

fun guessType(tableField: LuaTableField, context: SearchContext): ITy {
    //from comment
    val comment = tableField.comment
    if (comment != null) {
        val tyDef = comment.typeDef?.type
        if (tyDef != null) return tyDef
    }
    //guess from value
    val lastChild = tableField.lastChild
    if (lastChild is LuaExpr) {
        return lastChild.guessTypeFromCache(context)
    }
    return Ty.UNKNOWN
}

fun getName(tableField: LuaTableField): String? {
    val stub = tableField.stub
    if (stub != null)
        return stub.name
    val id = tableField.id
    if (id != null)
        return id.text

    val idExpr = tableField.idExpr
    if (idExpr is LuaLiteralExpr && idExpr.kind == LuaLiteralKind.String)
        return LuaString.getContent(idExpr.text).value
    return null
}

fun getFieldName(tableField: LuaTableField): String? {
    return getName(tableField)
}

fun getPresentation(tableField: LuaTableField): ItemPresentation {
    return object : ItemPresentation {
        override fun getPresentableText(): String? {
            return tableField.name
        }

        override fun getLocationString(): String {
            return tableField.containingFile.name
        }

        override fun getIcon(b: Boolean): Icon? {
            return LuaIcons.CLASS_FIELD
        }
    }
}

/**
 * xx['id']
 */
fun getIdExpr(tableField: LuaTableField): LuaLiteralExpr? {
    val bracket = tableField.lbrack
    if (bracket != null) {
        val nextLeaf = PsiTreeUtil.getNextSiblingOfType(bracket, LuaExpr::class.java)
        if (nextLeaf is LuaLiteralExpr && nextLeaf.kind == LuaLiteralKind.String)
            return nextLeaf
    }
    return null
}

fun toString(stubElement: StubBasedPsiElement<out StubElement<*>>): String {
    return "STUB:[" + stubElement.javaClass.simpleName + "]"
}

fun getPresentation(nameExpr: LuaNameExpr): ItemPresentation {
    return object : ItemPresentation {
        override fun getPresentableText(): String {
            return nameExpr.name
        }

        override fun getLocationString(): String {
            return nameExpr.containingFile.name
        }

        override fun getIcon(b: Boolean): Icon? {
            return LuaIcons.CLASS_FIELD
        }
    }
}

fun getNameIdentifier(ref: LuaNameExpr): PsiElement {
    return ref.id
}

fun getName(nameExpr: LuaNameExpr): String {
    val stub = nameExpr.stub
    if (stub != null)
        return stub.name
    return nameExpr.text
}

/**
 * 找出 LuaAssignStat 的第 index 位置上的 Var 的类型String
 * @param stat LuaAssignStat
 * *
 * @param index index
 * *
 * @return type string
 */
fun getTypeName(stat: LuaAssignStat, index: Int): String? {
    val expr = stat.getExprAt(index)
    var typeName: String? = null
    if (expr is LuaNameExpr) {
        // common 优先
        val comment = stat.comment
        if (comment != null) {
            val classDef = comment.classDef
            if (classDef != null) {
                typeName = classDef.name
            }
        }
        // 否则直接当成Global，以名字作类型
        if (typeName == null)
            typeName = expr.text
    }
    return typeName
}

fun guessReturnTypeSet(returnStat: LuaReturnStat?, index: Int, context: SearchContext): ITy {
    if (returnStat != null) {
        val returnExpr = returnStat.exprList
        if (returnExpr != null) {
            context.index = index
            return returnExpr.guessTypeAt(context)
        }
    }
    return Ty.UNKNOWN
}

fun getNameIdentifier(label: LuaLabelStat): PsiElement? {
    return label.id
}