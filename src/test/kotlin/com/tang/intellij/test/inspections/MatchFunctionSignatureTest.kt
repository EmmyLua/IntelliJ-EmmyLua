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

    fun testMultiReturn() = checkByText("""
        local function ret_nn()
            return 1, 2
        end
        local function ret_sn()
            return "1", 2
        end
        ---@param n1 number
        ---@param n2 number
        local function acp_nn(n1, n2) end

        acp_nn(ret_nn())
        acp_nn(<warning>ret_sn()</warning>)
        acp_nn(ret_nn(), 1)
        acp_nn(<warning>ret_sn()</warning>, 1)
    """)

    fun testParentIndex() = checkByText("""
        local dummy, A = 1, {}
        ---@return number, string
        function A.a()
            return 1, "1"
        end
        ---@param n number
        ---@param s string
        local function acp_ns(n, s) end

        acp_ns(A.a())
    """)
}