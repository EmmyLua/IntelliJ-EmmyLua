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

package com.tang.intellij.lua.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex

internal fun resolveFuncBodyOwner(ref: LuaNameExpr, context: SearchContext): LuaFuncBodyOwner? {
    var ret:LuaFuncBodyOwner? = null
    val refName = ref.name
    //local 函数名
    LuaPsiTreeUtilEx.walkUpLocalFuncDef(ref, Processor { localFuncDef ->
        if (refName == localFuncDef.name) {
            ret = localFuncDef
            return@Processor false
        }
        true
    })

    //global function
    if (ret == null) {
        val module = ref.moduleName ?: Constants.WORD_G
        LuaClassMemberIndex.process(module, refName, context, Processor {
            if (it is LuaFuncBodyOwner) {
                ret = it
                return@Processor false
            }
            true
        })
    }

    return ret
}

fun resolveLocal(ref: LuaNameExpr, context: SearchContext? = null) = resolveLocal(ref.name, ref, context)

private val KEY_RESOLVE = Key.create<CachedValue<PsiElement>>("lua.resolve.cache.resolveLocal")

fun resolveLocal(refName:String, ref: PsiElement, context: SearchContext? = null): PsiElement? {
    return CachedValuesManager.getCachedValue(ref, KEY_RESOLVE) {
        val element = resolveInFile(refName, ref, context)
        val r = if (element is LuaNameExpr) null else element
        CachedValueProvider.Result.create(r, ref)
    }
}

fun resolveInFile(refName:String, pin: PsiElement, context: SearchContext?): PsiElement? {
    var ret: PsiElement? = null
    var lastNameExpr: LuaNameExpr? = null

    //local/param
    LuaPsiTreeUtilEx.walkUpNameDef(pin, Processor { nameDef ->
        if (refName == nameDef.name)
            ret = nameDef
        ret == null
    }, Processor {
        for (expr in it.varExprList.exprList) {
            if (expr is LuaNameExpr && expr.name == refName) {
                lastNameExpr = expr
                return@Processor true
            }
        }
        true
    })

    ret = ret ?: lastNameExpr

    if (ret == null && refName == Constants.WORD_SELF) {
        val methodDef = PsiTreeUtil.getStubOrPsiParentOfType(pin, LuaClassMethodDef::class.java)
        if (methodDef != null && !methodDef.isStatic) {
            val methodName = methodDef.classMethodName
            val expr = methodName.expr
            ret = if (expr is LuaNameExpr && context != null && expr.name != Constants.WORD_SELF)
                resolve(expr, context)
            else
                expr
        }
    }
    return ret
}

fun isUpValue(ref: LuaNameExpr, context: SearchContext): Boolean {
    val funcBody = PsiTreeUtil.getParentOfType(ref, LuaFuncBody::class.java) ?: return false

    val refName = ref.name
    if (refName == Constants.WORD_SELF) {
        val classMethodFuncDef = PsiTreeUtil.getParentOfType(ref, LuaClassMethodDef::class.java)
        if (classMethodFuncDef != null && !classMethodFuncDef.isStatic) {
            val methodFuncBody = classMethodFuncDef.funcBody
            if (methodFuncBody != null)
                return methodFuncBody.textOffset < funcBody.textOffset
        }
    }

    val resolve = resolveLocal(ref, context)
    if (resolve != null) {
        if (!funcBody.textRange.contains(resolve.textRange))
            return true
    }

    return false
}

/**
 * 查找这个引用
 * @param nameExpr 要查找的ref
 * *
 * @param context context
 * *
 * @return PsiElement
 */
fun resolve(nameExpr: LuaNameExpr, context: SearchContext): PsiElement? {
    //search local
    var resolveResult = resolveInFile(nameExpr.name, nameExpr, context)

    //global
    if (resolveResult == null || resolveResult is LuaNameExpr) {
        val refName = nameExpr.name
        val moduleName = nameExpr.moduleName ?: Constants.WORD_G
        LuaClassMemberIndex.process(moduleName, refName, context, Processor {
            resolveResult = it
            false
        })
    }

    return resolveResult
}

fun multiResolve(ref: LuaNameExpr, context: SearchContext): Array<PsiElement> {
    val list = mutableListOf<PsiElement>()
    //search local
    val resolveResult = resolve(ref, context)
    if (resolveResult != null) {
        list.add(resolveResult)
    } else {
        val refName = ref.name
        val module = ref.moduleName ?: Constants.WORD_G
        LuaClassMemberIndex.process(module, refName, context, Processor {
            list.add(it)
            true
        })
    }
    return list.toTypedArray()
}

fun resolve(indexExpr: LuaIndexExpr, context: SearchContext): PsiElement? {
    val name = indexExpr.name ?: return null
    return resolve(indexExpr, name, context)
}

fun resolve(indexExpr: LuaIndexExpr, idString: String, context: SearchContext): PsiElement? {
    val type = indexExpr.guessParentType(context)
    var ret: PsiElement? = null
    type.eachTopClass(Processor { ty ->
        ret = ty.findMember(idString, context)
        if (ret != null)
            return@Processor false
        true
    })
    return ret
}

/**
 * 找到 require 的文件路径
 * @param pathString 参数字符串 require "aa.bb.cc"
 * *
 * @param project MyProject
 * *
 * @return PsiFile
 */
fun resolveRequireFile(pathString: String?, project: Project): LuaPsiFile? {
    if (pathString == null)
        return null
    val fileName = pathString.replace('.', '/')
    val f = LuaFileUtil.findFile(project, fileName)
    if (f != null) {
        val psiFile = PsiManager.getInstance(project).findFile(f)
        if (psiFile is LuaPsiFile)
            return psiFile
    }
    return null
}