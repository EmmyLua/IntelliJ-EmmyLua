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

package com.tang.intellij.test.structure

import com.intellij.testFramework.PlatformTestUtil
import com.intellij.util.ui.tree.TreeUtil
import com.tang.intellij.test.LuaTestBase
import org.intellij.lang.annotations.Language

class StructureViewTest : LuaTestBase() {

    fun testStructureViewBase() {
        doTest("""
             ---@class A
             local m = {}

             function m:method()end
        """.trimIndent(), """
            -main.lua
             -A
              method()
        """.trimIndent())
    }

    private fun doTest(@Language("lua") code: String, expected: String) {
        val normExpected = expected.trimIndent() + "\n"
        myFixture.configureByText("main.lua", code)
        myFixture.testStructureView {
            TreeUtil.expandAll(it.tree)
            PlatformTestUtil.assertTreeEqual(it.tree, normExpected)
        }
    }
}