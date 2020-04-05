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
        val targetElement: PsiElement?,
        val sourceElement: PsiElement,
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

    private fun contravariantOf(target: ITy, source: ITy, context: SearchContext, varianceFlags: Int, targetElement: PsiElement?, sourceElement: PsiElement, tyProblems: MutableMap<String, Collection<Problem>>): Boolean {
        if (target is ITyGeneric) {
            val base = TyAliasSubstitutor.substitute(target.base, context)

            if (base is ITyAlias) {
                TyUnion.each(base.ty.substitute(target.getMemberSubstitutor(context))) { concreteAliasTy ->
                    val problems = mutableListOf<Problem>()
                    tyProblems[concreteAliasTy.displayName] = problems

                    val isContravariant = contravariantOf(concreteAliasTy, source, context, varianceFlags, targetElement, sourceElement) {targetElement, sourceElement, message, highlightType ->
                        problems.add(Problem(targetElement, sourceElement, message, highlightType))
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

        if (base.flags and TyFlags.SHAPE != 0 && sourceElement is LuaTableExpr) {
            val sourceSubstitutor = source.getMemberSubstitutor(context)
            val targetSubstitutor = target.getMemberSubstitutor(context)

            target.processMembers(context, { _, targetMember ->
                val indexTy = targetMember.indexType?.getType()

                val sourceMember = if (indexTy != null) {
                    source.findIndexer(indexTy, context)
                } else {
                    targetMember.name?.let { source.findMember(it, context) }
                }

                val targetMemberTy = targetMember.guessType(context).let {
                    if (targetSubstitutor != null) it.substitute(targetSubstitutor) else it
                }

                if (sourceMember == null) {
                    if (TyUnion.find(targetMemberTy, TyNil::class.java) == null) {
                        isContravariant = false
                        val memberName = targetMember.name ?: "[${targetMember.indexType?.getType()?.displayName}]"
                        problems.add(Problem(
                                targetElement,
                                sourceElement,
                                "Type mismatch. Missing member: '%s' of: '%s'".format(memberName, target.displayName),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        ))
                    }

                    return@processMembers true
                }

                val sourceMemberTy = sourceMember.guessType(context).let {
                    if (sourceSubstitutor != null) it.substitute(sourceSubstitutor) else it
                }

                val memberElement = findHighlightElement(sourceMember.node.psi)

                if (memberElement is LuaTableExpr) {
                    isContravariant = contravariantOf(targetMemberTy, sourceMemberTy, context, varianceFlags, targetElement, memberElement) { targetElement, sourceElement, message, highlightType ->
                        problems.add(Problem(targetElement, sourceElement, message, highlightType))
                    }
                } else if (!targetMemberTy.contravariantOf(sourceMemberTy, context, varianceFlags)) {
                    isContravariant = false
                    problems.add(Problem(targetElement,
                            memberElement ?: sourceElement,
                            "Type mismatch. Required: '%s' Found: '%s'".format(targetMemberTy.displayName, sourceMemberTy.displayName),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING))
                }
                true
            }, true)
        } else if (!target.contravariantOf(source, context, varianceFlags)) {
            isContravariant = false
            problems.add(Problem(targetElement, sourceElement, "Type mismatch. Required: '%s' Found: '%s'".format(target.displayName, source.displayName), ProblemHighlightType.GENERIC_ERROR_OR_WARNING))
        }

        return isContravariant
    }

    fun contravariantOf(target: ITy, source: ITy, context: SearchContext, varianceFlags: Int, targetElement: PsiElement?, sourceElement: PsiElement, processProblem: (targetElement: PsiElement?, sourceElement: PsiElement, message: String, highlightType: ProblemHighlightType) -> Unit): Boolean {
        val tyProblems = mutableMapOf<String, Collection<Problem>>()
        val resolvedTarget = TyAliasSubstitutor.substitute(target, context)

        if (sourceElement is LuaTableExpr && acceptsShape(resolvedTarget, context)) {
            if (source is TyUnion && resolvedTarget.contravariantOf(source, context, varianceFlags)) {
                return true
            }

            TyUnion.each(resolvedTarget) {
                if (contravariantOf(it, source, context, varianceFlags, targetElement, sourceElement, tyProblems)) {
                    return true
                }
            }
        } else if (contravariantOf(resolvedTarget, source, context, varianceFlags, targetElement, sourceElement, tyProblems)) {
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
                val depth = getDepth(it.sourceElement, sourceElement)

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
                processProblem(it.targetElement, it.sourceElement, it.message, it.highlightType)
            }
        }

        return false
    }
}
