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

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.RecursionManager
import com.intellij.openapi.util.Ref
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaFileStub
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
class LuaFile(fileViewProvider: FileViewProvider) : PsiFileBase(fileViewProvider, LuaLanguage.INSTANCE) {

    override fun getFileType(): FileType {
        return LuaFileType.INSTANCE
    }

    val moduleName: String?
        get() {
            val greenStub = greenStub as? LuaFileStub
            return greenStub?.module ?: findModuleName()
        }

    fun findModuleName():String? {
        var child: PsiElement? = firstChild
        while (child != null) {
            if (child is LuaComment) { // ---@module name
                val classDef = PsiTreeUtil.getChildOfType(child, LuaDocClassDef::class.java)
                if (classDef != null && classDef.module != null) {
                    return classDef.name
                }
            } else if (child is LuaCallStat) { // module("name")
                val callExpr = child.expr as LuaCallExpr
                val expr = callExpr.expr
                if (expr is LuaNameExpr && expr.textMatches("module")) {
                    val stringArg = callExpr.firstStringArg
                    if (stringArg != null)
                        return stringArg.text
                }
            }
            child = child.nextSibling
        }
        return null
    }

    /**
     * 获取最后返回的类型
     * @return LuaTypeSet
     */
    fun getReturnedType(context: SearchContext): ITy {
        val greenStub = greenStub
        return (greenStub as? LuaFileStub)?.getReturnedType(context) ?: guessReturnedType(context)
    }

    fun guessReturnedType(context: SearchContext): ITy {
        return RecursionManager.doPreventingRecursion(this, true) {
            val set: ITy
            val lastChild = lastChild
            val returnStatRef = Ref.create<LuaReturnStat>()
            LuaPsiTreeUtil.walkTopLevelInFile(lastChild, LuaReturnStat::class.java) { luaReturnStat ->
                returnStatRef.set(luaReturnStat)
                false
            }
            set = LuaPsiImplUtil.guessReturnTypeSet(returnStatRef.get(), 0, context)
            set
        } ?: Ty.UNKNOWN
    }
}