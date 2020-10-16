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

class TestAlias : TestCompletionBase() {
    fun `test alias 1`() {
        myFixture.configureByFile("class.lua")
        doTest("""
            --- test_alias.lua

            ---@alias Handler fun(emmy: Emmy):void

            ---@param func Handler
            local function test(func)
            end

            test(function(value)
                value.--[[caret]]
            end)
        """) {
            assertTrue("sayHello" in it)
        }
    }
}