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
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.ty.*

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

fun resolveLocal(ref: LuaNameExpr, context: SearchContext?) = resolveLocal(ref.name, ref, context)

fun resolveLocal(refName:String, ref: PsiElement, context: SearchContext?): PsiElement? {
    val element = resolveInFile(refName, ref, context)
    if (element is LuaNameExpr)
        return null
    return element
}

fun resolveInFile(refName:String, pin: PsiElement, context: SearchContext?): PsiElement? {
    var ret: PsiElement? = null
    var lastName: LuaNameExpr? = null

    //local/param
    LuaPsiTreeUtilEx.walkUpNameDef(pin, Processor { nameDef ->
        if (refName == nameDef.name) {
            ret = nameDef
            return@Processor false
        }
        true
    }, Processor {
        if (refName == it.name) {
            lastName = it
            return@Processor false
        }
        true
    })

    if (ret == null)
        ret = lastName

    if (ret == null && refName == Constants.WORD_SELF) {
        val methodDef = PsiTreeUtil.getStubOrPsiParentOfType(pin, LuaClassMethodDef::class.java)
        if (methodDef != null && !methodDef.isStatic) {
            val methodName = methodDef.classMethodName
            val expr = methodName.expr
            ret = if (expr is LuaNameExpr && context != null)
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
    if (resolveResult == null) {
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
    TyUnion.process(type) { ty ->
        if (ty is TyClass) {
            ret = ty.findMember(idString, context)
            if (ret != null)
                return@process false
        }
        true
    }
    return ret
}

internal fun resolveType(nameDef: LuaNameDef, context: SearchContext): ITy {
    var type: ITy = Ty.UNKNOWN
    //作为函数参数，类型在函数注释里找
    if (nameDef is LuaParamNameDef) {
        type = resolveParamType(nameDef, context)
        //anonymous
        if (Ty.isInvalid(type))
            type = TyClass.createAnonymousType(nameDef)
    } else if (nameDef.parent is LuaTableField) {
        val field = nameDef.parent as LuaTableField
        val expr = PsiTreeUtil.findChildOfType(field, LuaExpr::class.java)
        if (expr != null) type = expr.guessType(context)
    } else {
        val docTy = nameDef.docTy
        if (docTy != null)
            return docTy

        val localDef = PsiTreeUtil.getParentOfType(nameDef, LuaLocalDef::class.java)
        if (localDef != null) {
            //计算 expr 返回类型
            if (Ty.isInvalid(type)) {
                val nameList = localDef.nameList
                val exprList = localDef.exprList
                if (nameList != null && exprList != null) {
                    context.index = localDef.getIndexFor(nameDef)
                    type = exprList.guessTypeAt(context)
                }
            }

            //anonymous
            type = type.union(TyClass.createAnonymousType(nameDef))
        }
    }
    return type
}

/**
 * 找参数的类型
 * @param paramNameDef param name
 * *
 * @param context SearchContext
 * *
 * @return LuaType
 */
fun resolveParamType(paramNameDef: LuaParamNameDef, context: SearchContext): ITy {
    val stubDocTy = paramNameDef.stub?.docTy
    if (stubDocTy != null)
        return stubDocTy

    val paramName = paramNameDef.name
    val paramOwner = PsiTreeUtil.getParentOfType(paramNameDef, LuaParametersOwner::class.java)

    // from comment
    val commentOwner = PsiTreeUtil.getParentOfType(paramNameDef, LuaCommentOwner::class.java)
    if (commentOwner != null) {
        val docTy = commentOwner.comment?.getParamDef(paramName)?.type
        if (docTy != null)
            return docTy
    }

    // 如果是个类方法，则有可能在父类里
    if (paramOwner is LuaClassMethodDef) {
        var classType: ITy? = paramOwner.guessClassType(context)
        val methodName = paramOwner.name
        while (classType != null) {
            classType = classType.getSuperClass(context)
            if (classType != null && methodName != null && classType is TyClass) {
                val superMethod = classType.findMember(methodName, context)
                if (superMethod is LuaClassMethod) {
                    val params = superMethod.params//todo : 优化
                    for (param in params) {
                        if (paramName == param.name) {
                            val types = param.ty
                            var set: ITy = Ty.UNKNOWN
                            TyUnion.each(types) { set = set.union(it) }
                            return set
                        }
                    }
                }
            }
        }
    }

    // module fun
    // function method(self) end
    if (paramOwner is LuaFuncDef && paramName == Constants.WORD_SELF) {
        val moduleName = paramNameDef.moduleName
        if (moduleName != null) {
            return TyLazyClass(moduleName)
        }
    }

    //for
    if (paramOwner is LuaForBStat) {
        val exprList = paramOwner.exprList
        val callExpr = PsiTreeUtil.findChildOfType(exprList, LuaCallExpr::class.java)
        val expr = callExpr?.expr
        if (expr != null) {
            val paramIndex = paramOwner.getIndexFor(paramNameDef)
            // ipairs
            if (expr.text == Constants.WORD_IPAIRS) {
                if (paramIndex == 0)
                    return Ty.NUMBER

                val args = callExpr.args
                if (args is LuaListArgs) {
                    val argExpr = PsiTreeUtil.findChildOfType(args, LuaExpr::class.java)
                    if (argExpr != null) {
                        val set = argExpr.guessType(context)
                        val tyArray = TyUnion.find(set, ITyArray::class.java)
                        if (tyArray != null)
                            return tyArray.base
                        val tyGeneric = TyUnion.find(set, ITyGeneric::class.java)
                        if (tyGeneric != null)
                            return tyGeneric.getParamTy(1)
                    }
                }
            }
            // pairs
            if (expr.text == Constants.WORD_PAIRS) {
                val args = callExpr.args
                if (args is LuaListArgs) {
                    val argExpr = PsiTreeUtil.findChildOfType(args, LuaExpr::class.java)
                    if (argExpr != null) {
                        val set = argExpr.guessType(context)
                        val tyGeneric = TyUnion.find(set, ITyGeneric::class.java)
                        if (tyGeneric != null)
                            return tyGeneric.getParamTy(paramIndex)
                        val tyArray = TyUnion.find(set, ITyArray::class.java)
                        if (tyArray != null)
                            return if (paramIndex == 0) Ty.NUMBER else tyArray.base
                    }
                }
            }
        }
    }
    /**
     * ---@param processor fun(p1:TYPE):void
     * local function test(processor)
     * end
     *
     * test(function(p1)  end)
     *
     * guess type for p1
     */
    if (paramOwner is LuaClosureExpr) {
        val p1 = paramOwner.parent as? LuaListArgs
        val p2 = p1?.parent as? LuaCallExpr
        if (p2 != null) {
            val type = p2.guessParentType(context)
            if (type is ITyFunction) {
                val args = p2.args
                if (args is LuaListArgs) {
                    val closureIndex = args.getIndexFor(paramOwner)
                    val paramTy = type.mainSignature.getParamTy(closureIndex)
                    if (paramTy is ITyFunction) {
                        val paramIndex = paramOwner.getIndexFor(paramNameDef)
                        return paramTy.mainSignature.getParamTy(paramIndex)
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