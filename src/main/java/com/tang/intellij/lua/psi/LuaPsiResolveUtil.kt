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
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.intellij.util.SmartList
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.reference.LuaReference
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
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

fun resolveLocal(ref: LuaNameExpr, context: SearchContext?): PsiElement? = resolveLocal(ref.name, ref, context)

fun resolveLocal(refName:String, ref: PsiElement, context: SearchContext?): PsiElement? {
    var ret: PsiElement? = null

    //local/param
    LuaPsiTreeUtil.walkUpLocalNameDef(ref) { nameDef ->
        if (refName == nameDef.name) {
            ret = nameDef
            return@walkUpLocalNameDef false
        }
        true
    }

    if (refName == Constants.WORD_SELF) {
        val block = PsiTreeUtil.getParentOfType(ref, LuaBlock::class.java)
        if (block != null) {
            val methodDef = PsiTreeUtil.getParentOfType(block, LuaClassMethodDef::class.java)
            if (methodDef != null && !methodDef.isStatic) {
                /**
                 * eg.
                 * function xx:aa()
                 *     local self = {}
                 *     return self
                 * end
                 */
                ret?.textRange?.let {
                    if (block.textRange.contains(it))
                        return ret
                }

                val expr = methodDef.classMethodName.expr
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
        val moduleName = ref.moduleName
        if (moduleName != null) {
            LuaClassMemberIndex.process(moduleName, refName, context, Processor {
                resolveResult = it
                false
            })
        }

        if (resolveResult == null) {
            LuaClassMemberIndex.process(Constants.WORD_G, refName, context, Processor {
                resolveResult = it
                false
            })
        }
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
        val module = ref.moduleName ?: Constants.WORD_G
        LuaClassMemberIndex.process(module, refName, context, Processor {
            list.add(it)
            true
        })
    }
    return list.toTypedArray()
}

fun resolve(indexExpr: LuaIndexExpr, context: SearchContext): PsiElement? {
    val id = indexExpr.id ?: return null
    return resolve(indexExpr, id.text, context)
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
        if (expr != null) type = expr.guessTypeFromCache(context)
    } else {
        val localDef = PsiTreeUtil.getParentOfType(nameDef, LuaLocalDef::class.java)
        if (localDef != null) {
            val comment = localDef.comment
            if (comment != null) {
                type = comment.guessType(context)
            }

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
private fun resolveParamType(paramNameDef: LuaParamNameDef, context: SearchContext): ITy {
    val owner = PsiTreeUtil.getParentOfType(paramNameDef, LuaCommentOwner::class.java)
    if (owner != null) {
        val paramName = paramNameDef.text
        val comment = owner.comment
        if (comment != null) {
            val paramDef = comment.getParamDef(paramName)
            if (paramDef != null) {
                return paramDef.type
            }
        }

        // 如果是个类方法，则有可能在父类里
        if (owner is LuaClassMethodDef) {
            var classType: ITy? = owner.guessClassType(context)
            val methodName = owner.name
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
        if (owner is LuaFuncDef && paramName == Constants.WORD_SELF) {
            val moduleName = paramNameDef.moduleName
            if (moduleName != null) {
                return TyLazyClass(moduleName)
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

                    val args = callExpr.args
                    if (args is LuaListArgs) {
                        val argExpr = PsiTreeUtil.findChildOfType(args, LuaExpr::class.java)
                        if (argExpr != null) {
                            val set = argExpr.guessTypeFromCache(context)
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
        if (owner is LuaCallStat) {
            val closure = LuaPsiTreeUtil.getParentOfType(paramNameDef, LuaClosureExpr::class.java, LuaFuncBody::class.java)
            if (closure != null) {
                val callExpr = owner.expr as LuaCallExpr
                val type = callExpr.guessParentType(context)
                //todo mainSignature ?
                if (type is ITyFunction) {
                    val args = callExpr.args
                    if (args is LuaListArgs) {
                        val closureIndex = args.getIndexFor(closure)
                        val paramTy = type.mainSignature.getParamTy(closureIndex)
                        if (paramTy is ITyFunction) {
                            val paramIndex = closure.getIndexFor(paramNameDef)
                            return paramTy.mainSignature.getParamTy(paramIndex)
                        }
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

private val GUESS_FROM_CACHE_KEY = Key.create<ParameterizedCachedValue<ITy, SearchContext>>("lua.ty.guess_from_cache")

fun LuaTypeGuessable.guessTypeFromCache(searchContext: SearchContext): ITy {
    //todo: 缓存有BUG，可能一直是旧的？
    /*if (searchContext.isDumb)
        return Ty.UNKNOWN
    val ty = CachedValuesManager.getManager(searchContext.project).getParameterizedCachedValue(this, GUESS_FROM_CACHE_KEY, { ctx ->
        val ty = guessType(ctx)
        CachedValueProvider.Result.create(ty, this)
    }, false, searchContext)
    return ty ?: Ty.UNKNOWN*/
    return this.guessType(searchContext)
}