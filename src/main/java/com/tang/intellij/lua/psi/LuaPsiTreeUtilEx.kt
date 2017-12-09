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
import com.intellij.psi.stubs.StubElement
import com.intellij.util.Processor

object LuaPsiTreeUtilEx {

    private fun StubElement<*>.walkUp(curChild: StubElement<*>, processor: Processor<StubElement<*>>) {
        val list = this.childrenStubs
        val index = list.indexOf(curChild)
        if (index > 0) {
            for (i in index until 0) {
                val element = list[i]
                if (!processor.process(element)) break
            }
        }
    }

    fun walkUpNameDef(psi: PsiElement, processor: Processor<LuaNameDef>) {
        if (psi is StubElement<*>) {
            val scope = psi.parentStub
            scope.walkUp(psi, Processor { cur ->

                true
            })
        } else LuaPsiTreeUtil.walkUpLocalNameDef(psi, processor)
    }
}