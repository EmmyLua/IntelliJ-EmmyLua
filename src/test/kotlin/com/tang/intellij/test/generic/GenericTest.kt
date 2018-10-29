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

package com.tang.intellij.test.generic

import com.tang.intellij.test.completion.TestCompletionBase

class GenericTest : TestCompletionBase() {

    fun `test generic 1`() {
        myFixture.configureByFile("class.lua")
        doTest("""
            --- test_generic.lua

            ---@generic T
            ---@param p1 T
            ---@return T
            local function test(p1)
                return p1
            end

            local value = test(emmy)
            value.--[[caret]]
        """) {
            assertTrue(it.contains("sayHello"))
        }
    }

    fun `test generic 2`() {
        myFixture.configureByFile("class.lua")
        doTest("""
            --- test_generic.lua

            ---@generic T
            ---@param p1 T
            ---@param func fun(value: T):void
            local function test(p1, func)
            end

            test(emmy, function(value)
                value.--[[caret]]
            end)
        """) {
            assertTrue(it.contains("sayHello"))
        }
    }

    fun `test generic 3`() {
        myFixture.configureByFile("class.lua")
        doTest("""
            --- test_generic.lua

            ---@class EmmyExt : Emmy
            local ext = {}

            function ext:reading() end

            ---@generic T
            ---@param p1 T
            ---@param func fun(value: T):void
            local function test(p1, func)
            end

            test(ext, function(value)
                value.--[[caret]]
            end)
        """) {
            assertTrue(it.contains("reading"))
        }
    }

    fun `test custom iterator`() {
        myFixture.configureByFile("class.lua")
        doTest("""
            --- test_generic.lua

            ---@generic T
            ---@param list T[]
            ---@return fun():number, T
            local function myIterator(list)
                local idx = 0
                return nil -- todo
            end

            ---@type Emmy[]
            local emmyList = {}

            for i, em in myIterator(emmyList) do
                em.--[[caret]]
            end
        """) {
            assertTrue(it.contains("sayHello"))
        }
    }
}
