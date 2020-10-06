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

package com.tang.intellij.test.editor

import com.tang.intellij.test.LuaTestBase
import com.tang.intellij.test.fileTreeFromText

class IssueTest : LuaTestBase() {

    fun `test issue #359` () {
        fileTreeFromText("""
            --- test_issue_359.lua
            
            ---@class TreeNode
            local m
            
            ---@return TreeNode
            function m:addText(tx)
            end
            
            --- test.lua
            ---@type TreeNode
            local node
            node:addText(1):addText(1)--[[caret]]
        """.trimIndent()).createAndOpenFileWithCaretMarker()

        myFixture.doHighlighting()
        myFixture.checkResultWithInlays("""
            ---@type TreeNode
            local node
            node:addText(<hint text="tx : "/>1):addText(<hint text="tx : "/>1)
        """.trimIndent())
    }

    fun `test issue #337@1`() {
        fileTreeFromText("""
            --- test_issue_337@1.lua
            a(function--[[caret]])
        """.trimIndent()).createAndOpenFileWithCaretMarker()
        myFixture.type('(')
        myFixture.checkResult("""
            a(function()  end)
        """.trimIndent())
    }

    fun `test issue #337@2`() {
        fileTreeFromText("""
            --- test_issue_337@2.lua
            a(func--[[caret]])
        """.trimIndent()).createAndOpenFileWithCaretMarker()
        myFixture.completeBasic()
        myFixture.checkResult("""
            a(function()  end)
        """.trimIndent())
    }
}