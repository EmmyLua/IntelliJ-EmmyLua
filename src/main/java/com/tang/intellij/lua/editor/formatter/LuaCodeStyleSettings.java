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

package com.tang.intellij.lua.editor.formatter;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.tang.intellij.lua.lang.LuaLanguage;

public class LuaCodeStyleSettings extends CustomCodeStyleSettings {
    public int call_arg_parentheses = 0;
    public int quote_style = 0;
    public boolean local_assign_continuation_align_to_first_expression = false;
    public boolean align_call_args = false;
    public boolean align_function_define_params = true;
    public boolean keep_one_space_between_table_and_bracket = true;
    public boolean align_table_field_to_first_field = false;
    public boolean keep_one_space_between_namedef_and_attribute = false;
    public boolean if_condition_align_with_each_other = false;
    public boolean continuous_assign_statement_align_to_equal_sign = true;
    public boolean continuous_assign_table_field_align_to_equal_sign = true;
    public boolean long_chain_expression_allow_one_space_after_colon = false;
    public boolean table_append_expression_no_space = false;
    public boolean label_no_indent = false;
    public boolean do_statement_no_indent = false;
    public boolean if_condition_no_continuation_indent = false;
    public int keep_line_after_if_statement = 0;
    public int keep_line_after_do_statement = 0;
    public int keep_line_after_while_statement = 0;
    public int keep_line_after_repeat_statement = 0;
    public int keep_line_after_for_statement = 0;
    public int keep_line_after_local_or_assign_statement = 3;
    public int keep_line_after_function_define_statement = 4;

    // old option
    public boolean SPACE_AFTER_TABLE_FIELD_SEP = true;
    public boolean SPACE_AROUND_BINARY_OPERATOR = true;
    public boolean SPACE_INSIDE_INLINE_TABLE = true;
    public boolean ALIGN_TABLE_FIELD_ASSIGN = false;

    LuaCodeStyleSettings(CodeStyleSettings container) {
        super(LuaLanguage.INSTANCE.getID(), container);
    }
}
