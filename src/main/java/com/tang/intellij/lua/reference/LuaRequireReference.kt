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

package com.tang.intellij.lua.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import com.tang.intellij.lua.lang.type.LuaString
import com.tang.intellij.lua.psi.LuaCallExpr
import com.tang.intellij.lua.psi.resolveRequireFile

/**
 *
 * Created by tangzx on 2016/12/9.
 */
class LuaRequireReference internal constructor(callExpr: LuaCallExpr) : PsiReferenceBase<LuaCallExpr>(callExpr) {

    private var pathString: String? = null
    private var range = TextRange.EMPTY_RANGE

    init {

        val path = callExpr.firstStringArg

        if (path != null && path.textLength > 2) {
            val luaString = LuaString.getContent(path.text)
            pathString = luaString.value

            if (pathString != null) {
                val start = path.textOffset - callExpr.textOffset + luaString.start
                val end = start + pathString!!.length
                range = TextRange(start, end)
            }
        }
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return myElement.manager.areElementsEquivalent(element, resolve())
    }

    override fun getRangeInElement(): TextRange {
        return range
    }

    override fun resolve(): PsiElement? {
        return if (pathString == null) null else resolveRequireFile(pathString, myElement.project)
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        /*val newPathString = pathString!! + FileUtil.getNameWithoutExtension(newElementName)
        var myText = myElement.text
        myText = myText.replace(pathString!!, newPathString)
        val element = LuaElementFactory.createWith(myElement.project, myText) as LuaCallStat
        return myElement.replace(element.expr)*/
        return myElement
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
