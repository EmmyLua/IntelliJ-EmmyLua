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

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.util.Processor
import com.tang.intellij.lua.stubs.LuaFileStub

typealias STUB_ELE = StubElement<*>
typealias STUB_PSI = StubBasedPsiElement<*>

object LuaPsiTreeUtilEx {

    private fun STUB_ELE.walkUp(curChild: STUB_ELE, processor: Processor<STUB_ELE>) {
        val list = this.childrenStubs
        val index = list.indexOf(curChild)
        if (index > 0) {
            for (i in 0 until index) {
                val element = list[index - i - 1]
                if (!processor.process(element)) break
            }
        }
    }

    private fun <T> findStubOfType(stub: STUB_ELE, clazz: Class<T>, collector: (t:T) -> Boolean) {
        val list = stub.childrenStubs
        for (i in 0 until list.size) {
            val stubElement = list[i]
            if (clazz.isInstance(stubElement.psi)) {
                val t = clazz.cast(stubElement.psi)
                collector(t)
            } else findStubOfType(stubElement, clazz, collector)
        }
    }

    fun walkUpNameDef(psi: PsiElement?, processor: Processor<PsiNamedElement>, nameExprProcessor: Processor<LuaNameExpr>? = null) {
        if (psi == null) return

        var continueSearch = true
        if (psi is STUB_PSI) {
            val stub = psi.stub
            if (stub != null) {
                var cur: STUB_ELE = stub
                do {
                    val scope = cur.parentStub
                    scope.walkUp(cur, Processor { next ->
                        val element = next.psi
                        when (element) {
                            is LuaLocalDef -> {
                                findStubOfType(next, LuaNameDef::class.java) {
                                    continueSearch = processor.process(it)
                                    continueSearch
                                }
                            }
                            is LuaParamNameDef -> {
                                continueSearch = processor.process(element)
                            }
                            else -> { }
                        }
                        continueSearch
                    })

                    if (scope is LuaFileStub)
                        break
                    cur = scope
                } while (continueSearch)

                continueSearch = false
            }
        }

        if (continueSearch)
            walkUpPsiLocalName(psi, processor, nameExprProcessor)
    }

    /**
     * 向上寻找 local 定义
     * @param element 当前搜索起点
     * @param processor 处理器
     */
    private fun walkUpPsiLocalName(element: PsiElement, processor: Processor<PsiNamedElement>, nameExprProcessor: Processor<LuaNameExpr>?) {
        var continueSearch = true

        var curr: PsiElement = element
        do {
            val next: PsiElement? = curr.prevSibling
            var isParent = false
            if (next == null) {
                curr = curr.parent
                isParent = true
            } else
                curr = next

            if (curr is LuaLocalDef) {
                // 跳过类似
                // local name = name //skip
                if (!curr.node.textRange.contains(element.node.textRange)) {
                    val nameList = curr.nameList
                    continueSearch = resolveInNameList(nameList, processor)
                }
            } else if (curr is LuaLocalFuncDef) {
                continueSearch = processor.process(curr)
            } else if (curr is LuaAssignStat && nameExprProcessor != null) {
                for (expr in curr.varExprList.exprList) {
                    if (expr is LuaNameExpr && expr != element) {
                        if (!nameExprProcessor.process(expr))
                            break
                    }
                }
            } else if (isParent) {
                when (curr) {
                    is LuaFuncBody -> continueSearch = resolveInFuncBody(curr, processor)
                    is LuaForAStat -> continueSearch = processor.process(curr.paramNameDef)
                    is LuaForBStat -> continueSearch = resolveInNameList(curr.paramNameDefList, processor)
                }
            }
        } while (continueSearch && curr !is PsiFile)
    }

    private fun resolveInFuncBody(funcBody: LuaFuncBody, processor: Processor<PsiNamedElement>): Boolean {
        for (parDef in funcBody.paramNameDefList) {
            if (!processor.process(parDef)) return false
        }
        return true
    }

    private fun resolveInNameList(nameList: LuaNameList?, processor: Processor<PsiNamedElement>): Boolean {
        if (nameList != null) {
            for (nameDef in nameList.nameDefList) {
                if (!processor.process(nameDef)) return false
            }
        }
        return true
    }

    private fun resolveInNameList(nameList: List<LuaParamNameDef>?, processor: Processor<PsiNamedElement>): Boolean {
        if (nameList != null) {
            for (nameDef in nameList) {
                if (!processor.process(nameDef)) return false
            }
        }
        return true
    }

    fun walkUpLocalFuncDef(psi: PsiElement, processor: Processor<LuaLocalFuncDef>) {
        var continueSearch = true
        if (psi is STUB_PSI) {
            val stub = psi.stub
            if (stub != null) {
                var cur: STUB_ELE = stub
                do {
                    val scope = cur.parentStub
                    scope.walkUp(cur, Processor { next ->
                        val psiElement = next.psi
                        if (psiElement is LuaLocalFuncDef) {
                            continueSearch = processor.process(psiElement)
                        }
                        continueSearch
                    })
                    if (scope is LuaFileStub)
                        break
                    cur = scope
                } while (continueSearch)
                continueSearch = false
            }
        }
        if (continueSearch)
            walkUpPsiLocalFunc(psi, processor)
    }

    /**
     * 向上寻找 local function 定义
     * @param current 当前搜导起点
     * @param processor 处理器
     */
    private fun walkUpPsiLocalFunc(current: PsiElement, processor: Processor<LuaLocalFuncDef>) {
        var continueSearch = true
        var curr = current
        do {
            if (curr is LuaLocalFuncDef)
                continueSearch = processor.process(curr)

            curr = curr.prevSibling ?: curr.parent
        } while (continueSearch && curr !is PsiFile)
    }

    fun processChildren(element: PsiElement?, processor: Processor<PsiElement>) {
        var child = element?.firstChild
        while (child != null) {
            if (!processor.process(child)) {
                break
            }
            child = child.nextSibling
        }
    }
}