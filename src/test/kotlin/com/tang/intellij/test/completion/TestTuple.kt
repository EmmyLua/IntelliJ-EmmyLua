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

class TestTuple : TestCompletionBase() {

    fun `test tuple 1`() {
        doTest("""
            --- test_tuple_1.lua

            ---@class Type1
            local obj = { name = "name" }

            ---@return number, Type1
            local function getTuple()
            end

            local a, b = getTuple()
            b.--[[caret]]
        """) {
            assertTrue("name" in it)
        }
    }
}