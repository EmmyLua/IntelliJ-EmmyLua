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
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.ext.recursionGuard
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaFileStub
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyLazyClass

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
class LuaPsiFile(fileViewProvider: FileViewProvider) : PsiFileBase(fileViewProvider, LuaLanguage.INSTANCE), LuaTypeGuessable {

    override fun getFileType(): FileType {
        return LuaFileType.INSTANCE
    }

    override fun setName(name: String): PsiElement {
        return if (FileUtil.getNameWithoutExtension(name) == name) {
            super.setName("$name.${LuaFileType.INSTANCE.defaultExtension}")
        } else super.setName(name)
    }

    val moduleName: String?
        get() {
            val stub = stub as? LuaFileStub
            return if (stub != null) stub.module else findModuleName()
        }

    private fun findModuleName():String? {
        var child: PsiElement? = firstChild
        while (child != null) {
            if (child is LuaComment) { // ---@module name
                val name = child.moduleName
                if (name != null) return name
            } else if (child is LuaStatement) {
                val comment = child.comment
                if (comment != null) {
                    val name = comment.moduleName
                    if (name != null) return name
                }
                if (child is LuaCallStat) { // module("name")
                    val callExpr = child.expr as LuaCallExpr
                    val expr = callExpr.expr
                    if (expr is LuaNameExpr && expr.textMatches(Constants.WORD_MODULE)) {
                        val stringArg = callExpr.firstStringArg
                        if (stringArg != null)
                            return stringArg.text
                    }
                }
            }
            child = child.nextSibling
        }
        return null
    }

    /**
     * 获取最后返回的类型
     * @return LuaType
     */
    override fun guessType(context: SearchContext): ITy {
        return recursionGuard(this, Computable {
            val moduleName = this.moduleName
            if (moduleName != null)
                TyLazyClass(moduleName)
            else {
                val stub = this.stub
                if (stub != null) {
                    val statStub = stub.childrenStubs.lastOrNull { it.psi is LuaReturnStat }
                    val stat = statStub?.psi
                    if (stat is LuaReturnStat)
                        guessReturnType(stat, 0, context)
                    else null
                } else {
                    val lastChild = lastChild
                    var stat: LuaReturnStat? = null
                    LuaPsiTreeUtil.walkTopLevelInFile(lastChild, LuaReturnStat::class.java, {
                        stat = it
                        false
                    })
                    if (stat != null)
                        guessReturnType(stat, 0, context)
                    else null
                }
            }
        }) ?: Ty.UNKNOWN
    }
}