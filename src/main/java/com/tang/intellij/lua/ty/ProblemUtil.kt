/*
 * Copyright (c) 2020
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

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

/*
 * Copyright (c) 2020
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


object ProblemUtil {
    private fun findHighlightElement(element: PsiElement): PsiElement? {
        return when (element) {
            is LuaLiteralExpr -> element
            is LuaTableExpr -> element
            is LuaParenExpr -> {
                return element.expr?.let { findHighlightElement(it) }
            }
            is LuaTableField -> {
                val valueExpr = element.exprList.last()
                return if (valueExpr is LuaParenExpr) findHighlightElement(valueExpr) else valueExpr
            }
            else -> null
        }
    }

    fun contravariantOf(target: ITy, source: ITy, context: SearchContext, varianceFlags: Int, element: PsiElement, processProblem: (element: PsiElement, message: String, highlightType: ProblemHighlightType) -> Unit): Boolean {
        val base = if (target is TyGeneric) target.base else target

        if (base is TyClass) {
            base.lazyInit(context)
        }

        var isContravariant = true

        if (base.flags and TyFlags.SHAPE != 0 && element is LuaTableExpr) {
            val parameterSubstitutor = if (target is ITyGeneric) target.getParameterSubstitutor(context) else null

            target.processMembers(context, { _, classMember ->
                val memberName = classMember.name

                if (memberName == null) {
                    return@processMembers true
                }

                val sourceMember = source.findMember(memberName, context)

                if (sourceMember == null) {
                    isContravariant = false
                    processProblem(element, "Type mismatch. Missing member: '%s'".format(memberName), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
                    return@processMembers true
                }

                var targetMemberTy = classMember.guessType(context)

                if (parameterSubstitutor != null) {
                    targetMemberTy = targetMemberTy.substitute(parameterSubstitutor)
                }

                val sourceMemberTy = sourceMember.guessType(context)
                val memberElement = findHighlightElement(sourceMember.node.psi)

                if (memberElement is LuaTableExpr) {
                    contravariantOf(targetMemberTy, sourceMemberTy, context, varianceFlags, memberElement, processProblem)
                } else if (!targetMemberTy.contravariantOf(sourceMemberTy, context, varianceFlags)) {
                    isContravariant = false
                    processProblem(memberElement ?: element, "Type mismatch. Required: '%s' Found: '%s'".format(targetMemberTy.displayName, sourceMemberTy.displayName), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
                }
                true
            }, true)
        } else if (!target.contravariantOf(source, context, varianceFlags)) {
            isContravariant = false
            processProblem(element, "Type mismatch. Required: '%s' Found: '%s'".format(target.displayName, source.displayName), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        }

        return isContravariant
    }
}
