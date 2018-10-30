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

package com.tang.intellij.test.vararg

import com.tang.intellij.test.completion.TestCompletionBase

class VarargTest : TestCompletionBase() {
    fun `test vararg 1`() {
        myFixture.configureByFile("class.lua")
        doTest("""
            --- test_vararg_1.lua
            
            ---@generic T
            ---@param index number|string
            ---@vararg T
            ---@return T
            local function select(index, ...)
            end

            ---@type Emmy
            local emmy = {}

            local r = select(1, emmy, emmy, emmy)
            r.--[[caret]]
        """) {
            assertTrue(it.contains("sayHello"))
        }
    }

    fun `test vararg 2`() {
        myFixture.configureByFile("class.lua")
        doTest("""
            --- test_vararg_2.lua

            ---@vararg Emmy
            local function test(...)
                local t = {...}
                t[1].--[[caret]]
            end
        """) {
            assertTrue(it.contains("sayHello"))
        }
    }
}