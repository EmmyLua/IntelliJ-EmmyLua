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
import com.intellij.util.SmartList
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.reference.LuaReference
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaGlobalIndex
import com.tang.intellij.lua.ty.*

internal fun resolveFuncBodyOwner(ref: LuaNameExpr, context: SearchContext): LuaFuncBodyOwner? {
    var ret:LuaFuncBodyOwner? = null
    val refName = ref.name
    //local 函数名
    LuaPsiTreeUtil.walkUpLocalFuncDef(ref) { localFuncDef ->
        if (refName == localFuncDef.name) {
            ret = localFuncDef
            return@walkUpLocalFuncDef false
        }
        true
    }

    //global function
    if (ret == null) {
        val global = LuaGlobalIndex.find(refName, context)
        if (global is LuaFuncBodyOwner) ret = global
    }

    return ret
}

fun resolveLocal(ref: LuaNameExpr, context: SearchContext?): PsiElement? {
    var ret: PsiElement? = null
    val refName = ref.name

    if (refName == Constants.WORD_SELF) {
        val block = PsiTreeUtil.getParentOfType(ref, LuaBlock::class.java)
        if (block != null) {
            val classMethodFuncDef = PsiTreeUtil.getParentOfType(block, LuaClassMethodDef::class.java)
            if (classMethodFuncDef != null && !classMethodFuncDef.isStatic) {
                val expr = classMethodFuncDef.classMethodName.expr
                val reference = expr.reference
                if (reference is LuaReference && context != null) {
                    val resolve = reference.resolve(context)
                    ret = resolve
                }
                if (ret == null && expr is LuaNameExpr)
                    ret = expr
            }
        }
    }

    //local 变量, 参数
    if (ret == null) {
        LuaPsiTreeUtil.walkUpLocalNameDef(ref) { nameDef ->
            if (refName == nameDef.name) {
                ret = nameDef
                return@walkUpLocalNameDef false
            }
            true
        }
    }

    //local 函数名
    if (ret == null) {
        LuaPsiTreeUtil.walkUpLocalFuncDef(ref) { nameDef ->
            val name = nameDef.name
            if (refName == name) {
                ret = nameDef
                return@walkUpLocalFuncDef false
            }
            true
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
 * @param ref 要查找的ref
 * *
 * @param context context
 * *
 * @return PsiElement
 */
fun resolve(ref: LuaNameExpr, context: SearchContext): PsiElement? {
    //search local
    var resolveResult = resolveLocal(ref, context)

    val refName = ref.name
    //global
    if (resolveResult == null) {
        resolveResult = LuaGlobalIndex.find(refName, context)
    }

    return resolveResult
}

fun multiResolve(ref: LuaNameExpr, context: SearchContext): Array<PsiElement> {
    val list = SmartList<PsiElement>()
    //search local
    val resolveResult = resolveLocal(ref, context)
    if (resolveResult != null) {
        list.add(resolveResult)
    } else {
        val refName = ref.name
        //global field
        val globalVars = LuaGlobalIndex.findAll(refName, context)
        list.addAll(globalVars)
    }
    return list.toTypedArray()
}

fun resolve(indexExpr: LuaIndexExpr, context: SearchContext): PsiElement? {
    val id = indexExpr.id ?: return null
    return resolve(indexExpr, id.text, context)
}

fun resolve(indexExpr: LuaIndexExpr, idString: String, context: SearchContext): PsiElement? {
    val typeSet = indexExpr.guessPrefixType(context)
    var ret: PsiElement? = null
    TyUnion.process(typeSet) { type ->
        if (type is TyClass) {
            //属性
            ret = type.findField(idString, context)
            if (ret != null)
                return@process false
            //方法
            ret = type.findMethod(idString, context)
            if (ret != null)
                return@process false
        }
        true
    }
    return ret
}

internal fun resolveType(nameDef: LuaNameDef, context: SearchContext): Ty {
    var typeSet: Ty? = null
    //作为函数参数，类型在函数注释里找
    if (nameDef is LuaParamNameDef) {
        typeSet = resolveParamType(nameDef, context)
    } else if (nameDef.parent is LuaTableField) {
        val field = nameDef.parent as LuaTableField
        val expr = PsiTreeUtil.findChildOfType(field, LuaExpr::class.java)
        if (expr != null) typeSet = expr.guessType(context)
    } else {
        val localDef = PsiTreeUtil.getParentOfType(nameDef, LuaLocalDef::class.java)
        if (localDef != null) {
            val comment = localDef.comment
            if (comment != null) {
                typeSet = comment.guessType(context)
            }

            //计算 expr 返回类型
            if (Ty.isInvalid(typeSet)) {
                val nameList = localDef.nameList
                val exprList = localDef.exprList
                if (nameList != null && exprList != null) {
                    context.index = localDef.getIndexFor(nameDef)
                    typeSet = exprList.guessTypeAt(context)
                }
            }

            //anonymous
            if (Ty.isInvalid(typeSet))
                typeSet = TyClass.createAnonymousType(nameDef)
        }
    }//变量声明，local x = 0
    //在Table字段里

    /*if (typeSet != null) {
        if (context.isGuessTypeKind(GuessTypeKind.FromName)) {
            val str = nameDef.text
            if (str.length > 2) {
                val matcher = CamelHumpMatcher(str, false)
                LuaClassIndex.getInstance().processAllKeys(context.project) { cls ->
                    if (matcher.prefixMatches(cls)) {
                        val type = LuaType.create(cls, null)
                        type.isUnreliable = true
                        typeSet = TySet.union(typeSet, type)
                    }
                    true
                }
            }
        }
    }*/
    return typeSet ?: Ty.UNKNOWN
}

/**
 * 找参数的类型
 * @param paramNameDef param name
 * *
 * @param context SearchContext
 * *
 * @return LuaTypeSet
 */
private fun resolveParamType(paramNameDef: LuaParamNameDef, context: SearchContext): Ty {
    val owner = PsiTreeUtil.getParentOfType(paramNameDef, LuaCommentOwner::class.java)
    if (owner != null) {
        val paramName = paramNameDef.text
        val comment = owner.comment
        if (comment != null) {
            val paramDef = comment.getParamDef(paramName)
            if (paramDef != null) {
                return paramDef.guessType(context)
            }
        }

        // 如果是个类方法，则有可能在父类里
        if (owner is LuaClassMethodDef) {
            var classType = owner.getClassType(context)
            val methodName = owner.name
            while (classType != null) {
                classType = classType.getSuperClass(context)
                if (classType != null && methodName != null) {
                    val superMethod = classType.findMethod(methodName, context)
                    if (superMethod != null) {
                        val params = superMethod.params//todo : 优化
                        for (param in params) {
                            if (paramName == param.name) {
                                val types = param.types
                                if (types.isNotEmpty()) {
                                    var set: Ty = Ty.UNKNOWN
                                    for (type in types) {
                                        set = set.union(TySerializedClass(type))
                                    }
                                    return set
                                }
                            }
                        }
                    }
                }
            }
        }

        //for
        if (owner is LuaForBStat) {
            val exprList = owner.exprList
            val callExpr = PsiTreeUtil.findChildOfType(exprList, LuaCallExpr::class.java)
            val expr = callExpr?.expr
            if (expr != null) {
                val paramIndex = owner.getIndexFor(paramNameDef)
                // ipairs
                if (expr.text == Constants.WORD_IPAIRS) {
                    if (paramIndex == 0)
                        return Ty.NUMBER

                    val argExprList = callExpr.args.exprList
                    val argExpr = PsiTreeUtil.findChildOfType(argExprList, LuaExpr::class.java)
                    if (argExpr != null) {
                        val set = argExpr.guessType(context)
                        val tyArray = TyUnion.find(set, TyArray::class.java)
                        if (tyArray != null)
                            return tyArray.base
                    }
                }
                // pairs
                if (expr.text == Constants.WORD_PAIRS) {
                    val argExprList = callExpr.args.exprList
                    val argExpr = PsiTreeUtil.findChildOfType(argExprList, LuaExpr::class.java)
                    if (argExpr != null) {
                        val set = argExpr.guessType(context)
                        val tyGeneric = TyUnion.find(set, TyGeneric::class.java)
                        if (tyGeneric != null)
                            return tyGeneric.getParamTy(paramIndex)
                    }
                }
            }
        }
    }
    return Ty.UNKNOWN
}

/**
 * 找到 require 的文件路径
 * @param pathString 参数字符串 require "aa.bb.cc"
 * *
 * @param project MyProject
 * *
 * @return PsiFile
 */
fun resolveRequireFile(pathString: String?, project: Project): LuaFile? {
    if (pathString == null)
        return null
    val fileName = pathString.replace('.', '/')
    val f = LuaFileUtil.findFile(project, fileName)
    if (f != null) {
        val psiFile = PsiManager.getInstance(project).findFile(f)
        if (psiFile is LuaFile)
            return psiFile
    }
    return null
}

fun getAnonymousType(element: PsiElement): String {
    val fileName = element.containingFile.name
    val startOffset = element.node.startOffset

    return String.format("%s@(%d)%s", fileName, startOffset, element.text)
}