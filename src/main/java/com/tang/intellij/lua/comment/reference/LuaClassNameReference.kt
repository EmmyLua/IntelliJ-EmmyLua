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

package com.tang.intellij.lua.comment.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.ProjectAndLibrariesScope
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocClassNameRef
import com.tang.intellij.lua.comment.psi.LuaDocGenericDef
import com.tang.intellij.lua.psi.LuaElementFactory
import com.tang.intellij.lua.stubs.index.LuaClassIndex

/**

 * Created by TangZX on 2016/11/29.
 */
class LuaClassNameReference(element: LuaDocClassNameRef) : PsiReferenceBase<LuaDocClassNameRef>(element) {

    override fun getRangeInElement() = TextRange(0, myElement.textLength)

    override fun isReferenceTo(element: PsiElement): Boolean {
        return myElement.manager.areElementsEquivalent(element, resolve())
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        val element = LuaElementFactory.createWith(myElement.project, "---@type $newElementName")
        val classNameRef = PsiTreeUtil.findChildOfType(element, LuaDocClassNameRef::class.java)
        return myElement.replace(classNameRef!!)
    }

    override fun resolve(): PsiElement? {
        val name = myElement.text
        // generic ?
        val comment = LuaCommentUtil.findContainer(myElement)
        for (genericDef in comment.findTags(LuaDocGenericDef::class.java)) {
            if (genericDef.name == name)
                return genericDef
        }
        return LuaClassIndex.find(name, myElement.project, ProjectAndLibrariesScope(myElement.project))
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
