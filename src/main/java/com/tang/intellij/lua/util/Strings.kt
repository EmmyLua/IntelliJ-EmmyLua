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

package com.tang.intellij.lua.util

import org.jetbrains.annotations.Contract

class Strings {
    companion object {
        @Contract(pure = true)
        fun stringHashCode(chars: CharSequence, from: Int, to: Int): Int {
            return stringHashCode(chars, from, to, 0)
        }

        @Contract(pure = true)
        fun stringHashCode(chars: CharSequence, from: Int, to: Int, prefixHash: Int): Int {
            var h = prefixHash
            for (off in from until to) {
                h = 31 * h + chars[off].code
            }
            return h
        }

        @Contract(pure = true)
        fun stringHashCode(chars: CharArray, from: Int, to: Int): Int {
            var h = 0
            for (off in from until to) {
                h = 31 * h + chars[off].code
            }
            return h
        }
    }
}

fun StringBuilder.appendLine(line: String): StringBuilder = append(line).append("\n")