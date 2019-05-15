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

package com.tang.intellij.test.codeInsight.ctrlFlow

import com.tang.intellij.lua.codeInsight.ctrlFlow.values.Point
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.Range
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertTrue

class RangeTest {
    @Test
    fun `test point 1`() {
        val p = Point(1)
        assertEquals(p.min, 1)
        assertEquals(p.max, 1)
        assertEquals(p.value, 1)
    }

    @Test
    fun `test point intersect set` () {
        val p = Point(1)
        val set = Range.range(-10, 10)
        val p2 = p.intersect(set)
        assertTrue { p2 is Point && p2.value == 1L }
    }

    @Test
    fun `test point subtract set` () {
        val p = Point(1)
        val set = Range.range(-10, 10)
        val p2 = p.subtract(set)
        assertTrue { p2.isEmpty }
    }
}