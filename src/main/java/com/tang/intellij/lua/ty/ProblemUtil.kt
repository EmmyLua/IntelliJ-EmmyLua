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

package com.tang.intellij.lua.ty

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.psi.LuaLiteralExpr
import com.tang.intellij.lua.psi.LuaParenExpr
import com.tang.intellij.lua.psi.LuaTableExpr
import com.tang.intellij.lua.psi.LuaTableField
import com.tang.intellij.lua.search.SearchContext

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


class Problem (
        val element: PsiElement,
        val message: String,
        val highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
)

// PsiTreeUtil has getDepth but was only introduced in IntelliJ 192.4787.16, we presently support 172.0
private fun getDepth(element: PsiElement, topLevel: PsiElement?): Int {
    var depth = 0
    var parent: PsiElement? = element

    while (parent !== topLevel && parent != null) {
        ++depth
        parent = parent.parent
    }

    return depth
}

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

    private fun acceptsShape(target: ITy, context: SearchContext): Boolean {
        TyUnion.each(target) {
            if (it is ITyGeneric) {
                if (acceptsShape(it.base, context)) {
                    return true
                }
            } else {
                if (it is TyClass) {
                    it.lazyInit(context)
                }

                if (it.flags and TyFlags.SHAPE != 0) {
                    return true
                }
            }
        }

        return false
    }

    private fun contravariantOf(target: ITy, source: ITy, context: SearchContext, varianceFlags: Int, element: PsiElement, tyProblems: MutableMap<String, Collection<Problem>>): Boolean {
        if (target is ITyGeneric) {
            val base = TyAliasSubstitutor.substitute(target.base, context)

            if (base is ITyAlias) {
                TyUnion.each(base.ty.substitute(target.getParameterSubstitutor(context))) { concreteAliasTy ->
                    val problems = mutableListOf<Problem>()
                    tyProblems[concreteAliasTy.displayName] = problems

                    val isContravariant = contravariantOf(concreteAliasTy, source, context, varianceFlags, element) { element, message, highlightType ->
                        problems.add(Problem(element, message, highlightType))
                    }

                    if (isContravariant) {
                        return true
                    }
                }

                return false
            }
        }

        val problems = mutableListOf<Problem>()
        tyProblems[target.displayName] = problems

        val base = if (target is ITyGeneric) target.base else target

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

                var targetMemberTy = classMember.guessType(context)
                val sourceMember = source.findMember(memberName, context)

                if (sourceMember == null) {
                    if (TyUnion.find(targetMemberTy, TyNil::class.java) == null) {
                        isContravariant = false
                        problems.add(Problem(element, "Type mismatch. Missing member: '%s' of: '%s'".format(memberName, target.displayName), ProblemHighlightType.GENERIC_ERROR_OR_WARNING))
                    }

                    return@processMembers true
                }

                val sourceMemberTy = sourceMember.guessType(context)

                if (parameterSubstitutor != null) {
                    targetMemberTy = targetMemberTy.substitute(parameterSubstitutor)
                }

                val memberElement = findHighlightElement(sourceMember.node.psi)

                if (memberElement is LuaTableExpr) {
                    isContravariant = contravariantOf(targetMemberTy, sourceMemberTy, context, varianceFlags, memberElement) { element, message, highlightType ->
                        problems.add(Problem(element, message, highlightType))
                    }
                } else if (!targetMemberTy.contravariantOf(sourceMemberTy, context, varianceFlags)) {
                    isContravariant = false
                    problems.add(Problem(memberElement ?: element,
                            "Type mismatch. Required: '%s' Found: '%s'".format(targetMemberTy.displayName, sourceMemberTy.displayName),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING))
                }
                true
            }, true)
        } else if (!target.contravariantOf(source, context, varianceFlags)) {
            isContravariant = false
            problems.add(Problem(element, "Type mismatch. Required: '%s' Found: '%s'".format(target.displayName, source.displayName), ProblemHighlightType.GENERIC_ERROR_OR_WARNING))
        }

        return isContravariant
    }

    fun contravariantOf(target: ITy, source: ITy, context: SearchContext, varianceFlags: Int, element: PsiElement, processProblem: (element: PsiElement, message: String, highlightType: ProblemHighlightType) -> Unit): Boolean {
        val tyProblems = mutableMapOf<String, Collection<Problem>>()
        val resolvedTarget = TyAliasSubstitutor.substitute(target, context)

        if (element is LuaTableExpr && acceptsShape(resolvedTarget, context)) {
            if (source is TyUnion && resolvedTarget.contravariantOf(source, context, varianceFlags)) {
                return true
            }

            TyUnion.each(resolvedTarget) {
                if (contravariantOf(it, source, context, varianceFlags, element, tyProblems)) {
                    return true
                }
            }
        } else if (contravariantOf(resolvedTarget, source, context, varianceFlags, element, tyProblems)) {
            return true
        }

        // We consider the best matches to be the types with the deepest nested problems.
        val bestMatchingCandidates = mutableListOf<String>()
        var bestMatchingMinDepth = -1

        tyProblems.forEach { candidate, candidateProblems ->
            if (candidateProblems.isEmpty()) {
                return@forEach
            }

            var candidateMinDepth = Int.MAX_VALUE

            candidateProblems.forEach {
                val depth = getDepth(it.element, element)

                if (depth < candidateMinDepth) {
                    candidateMinDepth = depth
                }
            }

            if (candidateMinDepth >= bestMatchingMinDepth) {
                if (candidateMinDepth > bestMatchingMinDepth) {
                    bestMatchingCandidates.clear()
                    bestMatchingMinDepth = candidateMinDepth
                }

                bestMatchingCandidates.add(candidate)
            }
        }

        bestMatchingCandidates.forEach { candidate ->
            tyProblems[candidate]?.forEach {
                processProblem(it.element, it.message, it.highlightType)
            }
        }

        return false
    }
}
