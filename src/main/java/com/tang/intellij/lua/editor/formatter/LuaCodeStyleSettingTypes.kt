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

package com.tang.intellij.lua.editor.formatter

class CodeStyleSettingsOptions {
    companion object {
        val quote_style_names = arrayOf("none", "single", "double")
        val quote_style_values = intArrayOf(0, 1, 2)

        val call_arg_parentheses_names = arrayOf("keep", "remove", "remove_table_only", "remove_string_only", "unambiguous_remove_string_only")
        val call_arg_parentheses_values = intArrayOf(0, 1, 2, 3, 4)

        val line_layout_names = arrayOf(
                "minLine:0",
                "minLine:1",
                "minLine:2",
                "keepLine",
                "keepLine:1",
                "keepLine:2"
        )

        val line_layout_values = intArrayOf(
                0, 1, 2, 3, 4, 5
        )
    }
}
