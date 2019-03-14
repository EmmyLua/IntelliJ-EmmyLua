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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaIndexExprType
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.ITyClass
import com.tang.intellij.lua.ty.TyUnion

fun resolveLocal(ref: LuaNameExpr, context: SearchContext? = null) = resolveLocal(ref.name, ref, context)

fun resolveLocal(refName:String, ref: PsiElement, context: SearchContext? = null): PsiElement? {
    val element = resolveInFile(refName, ref, context)
    return if (element is LuaNameExpr) null else element
}

fun resolveInFile(refName:String, pin: PsiElement, context: SearchContext?): PsiElement? {
    var ret: PsiElement? = null
    LuaDeclarationTree.get(pin.containingFile).walkUp(pin) { decl ->
        if (decl.name == refName)
            ret = decl.firstDeclaration.psi
        ret == null
    }

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
    val resolveResult = resolveInFile(ref.name, ref, context)
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
    val tree = LuaDeclarationTree.get(indexExpr.containingFile)
    val declaration = tree.find(indexExpr)
    if (declaration != null) {
        return declaration.psi
    }

    val type = indexExpr.guessParentType(context)
    var ret: PsiElement? = null
    type.eachTopClass(Processor { ty ->
        ret = ty.findMember(idString, context)
        if (ret != null)
            return@Processor false
        true
    })
    // for chain
    // add by clu on 2018-12-29 16:25:34 添加对无上下文的元素的支持t.data.name
    val typeByRoot = resolveTypeByRoot(indexExpr, idString, context)
    if (typeByRoot.isNotEmpty()) {
        return typeByRoot.first()
    }
    // end
    return ret
}

// for chain
/**
 * t.data.name，直接通过name的父类型找自己肯定找不到的，因为对t.data, name进行indexStub的时候，t.data类型还没有index，
 * 因此无法找到name的父类型t.data，自然就没有存储t.data和name的关系了，因此需要通过t找到t.data，然后再找t.__data找到name即可
 * 目前本方法只返回第一个PsiElement
 */
fun resolveTypeByRoot(indexExpr: LuaIndexExpr, idString: String, context: SearchContext): Array<PsiElement> {
    val all = mutableListOf<PsiElement>()
    LuaIndexExprType.getAllKnownIndexLuaExprType(indexExpr, context).forEach {
        val baseType = it.key
        val indexExprNames = it.value
        val matches = mutableListOf<PsiElement>()
        TyUnion.each(baseType, {
            if (it is ITyClass) {
                // it类型是：118@F_LuaTest_src_test_t.lua
                // 获取118@F_LuaTest_src_test_t.lua.__data类型的idString属性
                val parentClassNameOfCurrentIndexExpr = LuaIndexExprType.getFiledNameAsClassName(it.className, indexExprNames.toTypedArray(), indexExprNames.size)
                LuaClassMemberIndex.process(parentClassNameOfCurrentIndexExpr, idString, context, Processor {
                    if (it.text.endsWith("." + idString)) {
                        matches.add(it)
                    }
                    true
                })

                // 只要最短的
                if (matches.isNotEmpty()) {
                    // 只要1个
                    return@each
                }
            }
        })

        // 只要1个
        if (matches.isNotEmpty()) {
            matches.sortBy { it.text.length }
            all.add(matches.first())
            return@forEach
        }
    }
    return all.toTypedArray()
}
// end

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