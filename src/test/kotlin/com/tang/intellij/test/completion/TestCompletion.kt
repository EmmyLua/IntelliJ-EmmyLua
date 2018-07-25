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

import com.intellij.codeInsight.completion.CompletionType
import java.util.*

/**
 *
 * Created by tangzx on 2017/4/23.
 */
class TestCompletion : TestCompletionBase() {

    fun testLocalCompletion() {
        myFixture.configureByFiles("testCompletion.lua")
        myFixture.complete(CompletionType.BASIC, 1)
        val strings = myFixture.lookupElementStrings

        assertNotNull(strings)
        assertTrue(strings!!.containsAll(Arrays.asList("a", "b", "func1")))
    }

    fun testGlobalCompletion() {
        //test 1
        myFixture.configureByFiles("globals.lua")
        myFixture.configureByText("test.lua", "<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        var strings = myFixture.lookupElementStrings

        assertNotNull(strings)
        assertTrue(strings!!.containsAll(Arrays.asList("gVar1", "gVar2")))

        //test 2
        myFixture.configureByFiles("globals.lua")
        myFixture.configureByText("test.lua", "gVar2.<caret>")
        myFixture.complete(CompletionType.BASIC, 1)
        strings = myFixture.lookupElementStrings

        assertNotNull(strings)
        assertTrue(strings!!.containsAll(Arrays.asList("aaa", "bbb", "ccc")))
    }

    fun testSelfCompletion() {
        myFixture.configureByFiles("testSelf.lua")
        myFixture.complete(CompletionType.BASIC, 1)
        val strings = myFixture.lookupElementStrings

        assertNotNull(strings)
        assertTrue(strings!!.containsAll(Arrays.asList("self:aaa", "self:abb")))
    }

    fun testParamCompletion() {
        myFixture.configureByFiles("testParam.lua")
        myFixture.complete(CompletionType.BASIC, 1)
        val strings = myFixture.lookupElementStrings

        assertNotNull(strings)
        assertTrue(strings!!.containsAll(Arrays.asList("param1", "param2")))
    }

    fun testAnnotation() {
        val code = "---@class MyClass\n" +
                "---@field public name string\n" +
                "local s = {}\n" +
                "function s:method()end\n" +
                "function s.staticMethod()end\n" +
                "---@type MyClass\n" +
                "local instance\n"

        // fields and methods
        myFixture.configureByText("test.lua", code + "instance.<caret>")
        myFixture.completeBasic()
        var strings = myFixture.lookupElementStrings
        assertNotNull(strings)
        assertTrue(strings!!.containsAll(Arrays.asList("name", "method", "staticMethod")))


        // methods
        myFixture.configureByText("test.lua", code + "instance:<caret>")
        myFixture.completeBasic()
        strings = myFixture.lookupElementStrings
        assertNotNull(strings)
        assertTrue(strings!!.contains("method"))
    }

    fun testAnnotationArray() {
        myFixture.configureByFiles("class.lua", "testAnnotationArray.lua")
        myFixture.complete(CompletionType.BASIC, 1)
        val strings = myFixture.lookupElementStrings

        assertNotNull(strings)
        assertTrue(strings!!.containsAll(Arrays.asList("name", "age", "sayHello")))
    }

    fun testAnnotationFun() {
        myFixture.configureByFiles("class.lua", "testAnnotationFun.lua")
        myFixture.complete(CompletionType.BASIC)
        val strings = myFixture.lookupElementStrings

        assertNotNull(strings)
        assertTrue(strings!!.containsAll(Arrays.asList("name", "age", "sayHello")))
    }

    fun testAnnotationDict() {
        myFixture.configureByFiles("class.lua", "testAnnotationDict.lua")
        myFixture.complete(CompletionType.BASIC)
        val strings = myFixture.lookupElementStrings

        assertNotNull(strings)
        assertTrue(strings!!.containsAll(Arrays.asList("name", "age", "sayHello")))
    }

    fun testAnonymous() {
        doTest("""
            --- testAnonymous.lua

            local function test()
                local v = xx()
                v.pp = 123
                return v
            end
            local v = test()
            v.--[[caret]]
        """) {
            assertTrue(it.contains("pp"))
        }
    }

    fun `test doc table 1`() {
        doTest("""
             --- doc_table_test_A.lua

             ---@return { name:string, value:number }
             function getData() end

             --- doc_table_test_B.lua
             local a = getData()
             a.--[[caret]]
        """) {
            assertTrue(it.contains("name"))
        }
    }
}
