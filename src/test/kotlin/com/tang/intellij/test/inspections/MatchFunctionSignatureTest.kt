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

package com.tang.intellij.test.inspections

import com.tang.intellij.lua.codeInsight.inspection.MatchFunctionSignatureInspection

class MatchFunctionSignatureTest : LuaInspectionsTestBase(MatchFunctionSignatureInspection()) {
    fun testTypeMissMatch() = checkByText("""
        ---@param p1 number
        ---@param p2 string
        local function test(p1, p2)
        end

        test(1, <warning>2</warning>)
    """)

    fun testTooManyArgs() = checkByText("""
        ---@param p1 number
        ---@param p2 string
        local function test(p1, p2)
        end

        test(1, "2", <warning>3</warning>)
    """)

    fun testMissArgs() = checkByText("""
        local function test(p1, p2)
        end
        test(1<warning>)</warning>
    """)
}