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

class TestEnum : TestCompletionBase() {

    fun `test enum members via type annotation`() {
        doTest("""
            --- test_enum_members.lua

            ---@enum Color
            local Color = {
                Red = 1,
                Green = 2,
                Blue = 3,
            }

            ---@type Color
            local color

            color.--[[caret]]
        """) {
            assertTrue(it.containsAll(listOf("Red", "Green", "Blue")))
        }
    }

    fun `test enum members via param annotation`() {
        doTest("""
            --- test_enum_param.lua

            ---@enum Color
            local Color = { Red = 1, Green = 2 }

            ---@param color Color
            local function paint(color)
                color.--[[caret]]
            end
        """) {
            assertTrue(it.containsAll(listOf("Red", "Green")))
        }
    }

    fun `test enum across files`() {
        doTest("""
            --- colors.lua

            ---@enum Color
            local Color = { Red = 1, Green = 2 }

            --- test_enum_cross_file.lua

            ---@type Color
            local color

            color.--[[caret]]
        """) {
            assertTrue(it.containsAll(listOf("Red", "Green")))
        }
    }

    fun `test enum key modifier`() {
        doTest("""
            --- test_enum_key.lua

            ---@enum (key) Color
            local Color = { Red = true, Green = true }

            ---@type Color
            local color

            color.--[[caret]]
        """) {
            assertTrue(it.containsAll(listOf("Red", "Green")))
        }
    }

    fun `test enum name completion in type reference`() {
        doTest("""
            --- test_enum_type_name.lua

            ---@enum Color
            local Color = { Red = 1 }

            ---@type Col--[[caret]]
        """) {
            assertTrue("Color" in it)
        }
    }
}
