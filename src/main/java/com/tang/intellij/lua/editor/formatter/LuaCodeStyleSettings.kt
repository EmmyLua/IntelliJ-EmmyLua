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

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import com.tang.intellij.lua.lang.LuaLanguage

/**
 * LuaCodeStyleSettings
 * Created by tangzx on 2017/2/22.
 */
class LuaCodeStyleSettings(container: CodeStyleSettings?) :
    CustomCodeStyleSettings(LuaLanguage.INSTANCE.id, container) {
    var call_arg_parentheses = 0
    var quote_style = 0
    var local_assign_continuation_align_to_first_expression = false
    var align_call_args = false
    var align_function_define_params = true
    var keep_one_space_between_table_and_bracket = true
    var align_table_field_to_first_field = false
    var keep_one_space_between_namedef_and_attribute = false
    var if_condition_align_with_each_other = false
    var continuous_assign_statement_align_to_equal_sign = true
    var continuous_assign_table_field_align_to_equal_sign = true
    var long_chain_expression_allow_one_space_after_colon = false
    var table_append_expression_no_space = false
    var label_no_indent = false
    var do_statement_no_indent = false
    var if_condition_no_continuation_indent = false
    var keep_line_after_if_statement = 0
    var keep_line_after_do_statement = 0
    var keep_line_after_while_statement = 0
    var keep_line_after_repeat_statement = 0
    var keep_line_after_for_statement = 0
    var keep_line_after_local_or_assign_statement = 3
    var keep_line_after_function_define_statement = 4

    // old options
    var SPACE_AFTER_TABLE_FIELD_SEP = true
    var SPACE_AROUND_BINARY_OPERATOR = true
    var SPACE_INSIDE_INLINE_TABLE = true
    var ALIGN_TABLE_FIELD_ASSIGN = false

    // 设计上如果等于默认选项则不添加选项
    fun makeCommandLineParams(params: MutableList<String>) {
        when (call_arg_parentheses) {
            1 -> {
                params.add("--call_arg_parentheses=single")
            }
            2 -> {
                params.add("--call_arg_parentheses=double")
            }
        }

        when (quote_style) {
            1 -> {
                params.add("--quote_style=remove")
            }
            2 -> {
                params.add("--quote_style=remove_table_only")
            }
            3 -> {
                params.add("--quote_style=remove_string_only")
            }
            4 -> {
                params.add("--quote_style=unambiguous_remove_string_only")
            }
        }

        if (local_assign_continuation_align_to_first_expression) {
            params.add("--local_assign_continuation_align_to_first_expression=true")
        }

        if (align_call_args) {
            params.add("--align_call_args=true")
        }

        if (!align_function_define_params) {
            params.add("--align_function_define_params=false")
        }

        if (!keep_one_space_between_table_and_bracket) {
            params.add("--keep_one_space_between_table_and_bracket=false")
        }

        if (align_table_field_to_first_field) {
            params.add("--align_table_field_to_first_field=true")
        }

        if (keep_one_space_between_namedef_and_attribute) {
            params.add("--keep_one_space_between_namedef_and_attribute=true")
        }

        if (if_condition_align_with_each_other) {
            params.add("--if_condition_align_with_each_other=true")
        }

        if (!continuous_assign_statement_align_to_equal_sign) {
            params.add("--continuous_assign_statement_align_to_equal_sign=false")
        }

        if (!continuous_assign_table_field_align_to_equal_sign) {
            params.add("--continuous_assign_table_field_align_to_equal_sign=false")
        }

        if (long_chain_expression_allow_one_space_after_colon) {
            params.add("--long_chain_expression_allow_one_space_after_colon=true")
        }

        if (table_append_expression_no_space) {
            params.add("--table_append_expression_no_space=true")
        }

        if (label_no_indent) {
            params.add("--label_no_indent=true")
        }

        if (do_statement_no_indent) {
            params.add("--do_statement_no_indent=true")
        }

        if (if_condition_no_continuation_indent) {
            params.add("--if_condition_no_continuation_indent=true")
        }

        setLineLayout(params, keep_line_after_if_statement, "keep_line_after_if_statement", 0)
        setLineLayout(params, keep_line_after_do_statement, "keep_line_after_do_statement", 0)
        setLineLayout(params, keep_line_after_while_statement, "keep_line_after_while_statement", 0)
        setLineLayout(params, keep_line_after_repeat_statement, "keep_line_after_repeat_statement", 0)
        setLineLayout(params, keep_line_after_for_statement, "keep_line_after_for_statement", 0)
        setLineLayout(params, keep_line_after_local_or_assign_statement, "keep_line_after_local_or_assign_statement", 3)
        setLineLayout(params, keep_line_after_function_define_statement, "keep_line_after_function_define_statement", 4)
    }

    fun setLineLayout(params: MutableList<String>, field: Int, filedName: String, default: Int) {
        if (field == default) {
            return
        }

        when (field) {
            0 -> {
                params.add("--${filedName}=minLine:0")
            }
            1 -> {
                params.add("--${filedName}=minLine:1")
            }
            2 -> {
                params.add("--${filedName}=minLine:2")
            }
            3 -> {
                params.add("--${filedName}=keepLine")
            }
            4 -> {
                params.add("--${filedName}=keepLine:1")
            }
            5 -> {
                params.add("--${filedName}=keepLine:2")
            }
        }
    }


}