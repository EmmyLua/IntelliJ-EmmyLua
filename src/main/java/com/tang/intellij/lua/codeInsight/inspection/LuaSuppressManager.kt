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

package com.tang.intellij.lua.codeInsight.inspection

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.psi.LuaDocTagSuppress
import com.tang.intellij.lua.comment.psi.LuaDocTypes
import com.tang.intellij.lua.psi.LuaCommentOwner

class LuaSuppressManager : InspectionSuppressor {
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return emptyArray()
    }

    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        val commentOwner = if (element is LuaCommentOwner) element else PsiTreeUtil.getParentOfType(element, LuaCommentOwner::class.java)
        val suppress = commentOwner?.comment?.findTag(LuaDocTagSuppress::class.java)
        if (suppress != null) {
            var child = suppress.firstChild
            while (child != null) {
                if (child.node.elementType == LuaDocTypes.PROPERTY) {
                    if (child.textMatches(toolId)) {
                        return true
                    }
                }
                child = child.nextSibling
            }
        }
        return false
    }
}
