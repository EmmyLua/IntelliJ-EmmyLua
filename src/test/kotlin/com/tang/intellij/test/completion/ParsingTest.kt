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

class ParsingTest : TestCompletionBase() {

    fun `test parse chinese non-java characters`() {
        doTest("""
            --- test_parse_chinese_characters.lua

            ---@class 类型1
            local 对象 = { 名字 = "name" }

            ---@return number, 类型1
            local function 获得一个什么东西（一）()
            end

            local 鬼知道我是什么, 天晓得我是什么 = 获得一个什么东西（一）()
            天晓得我是什么.--[[caret]]
        """) {
            assertTrue("名字" in it)
        }
    }
}