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
            for (i in index until 0) {
                val element = list[i]
                if (!processor.process(element)) break
            }
        }
    }

    fun <T> findStubOfType(stub: STUB_ELE, clazz: Class<T>, collector: (t:T) -> Boolean) {
        val list = stub.childrenStubs
        for (i in 0 until list.size) {
            val stubElement = list[i]
            if (clazz.isInstance(stubElement.psi)) {
                val t = clazz.cast(stubElement.psi)
                collector(t)
            } else findStubOfType(stubElement, clazz, collector)
        }
    }

    fun <T> findStubOfType(stub: STUB_ELE, clazz: Class<T>): List<T> {
        val list = mutableListOf<T>()
        findStubOfType(stub, clazz) { psi ->
            list.add(psi)
            true
        }
        return list
    }

    fun walkUpNameDef(psi: PsiElement, processor: Processor<LuaNameDef>) {
        var continueSearch = true
        if (psi is STUB_PSI) {
            val stub = psi.stub
            if (stub != null) {
                var cur: STUB_ELE = stub
                do {
                    val scope = cur.parentStub
                    scope.walkUp(cur, Processor { next ->
                        when (next.psi) {
                            is LuaLocalDef -> {
                                findStubOfType(next, LuaNameDef::class.java) {
                                    continueSearch = !processor.process(it)
                                    continueSearch
                                }
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
            LuaPsiTreeUtil.walkUpLocalNameDef(psi, processor)
    }
}