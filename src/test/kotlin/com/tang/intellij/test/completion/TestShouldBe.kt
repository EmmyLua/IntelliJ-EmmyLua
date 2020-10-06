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

package com.tang.intellij.test.completion

import junit.framework.Assert

class TestShouldBe : TestCompletionBase() {

    fun `test issue #298`() {
        doTest("""
            --- test_issue_298.lua
            
            ---@overload fun(t:{aaa: string, bbb: string, ccc: string})
            ---@param aaa string
            ---@param bbb string
            ---@param ccc string
            ---@return number
            function test(aaa, bbb, ccc)
            end

            test {
                a--[[caret]]
            }
        """.trimIndent()) {
            Assert.assertTrue(it.contains("aaa = "))
        }
    }

}