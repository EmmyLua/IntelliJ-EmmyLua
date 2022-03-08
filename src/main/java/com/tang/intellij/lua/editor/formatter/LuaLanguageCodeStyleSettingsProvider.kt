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

import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.IndentOptionsEditor
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.tang.intellij.lua.lang.LuaLanguage

/**

 * Created by tangzx on 2017/2/22.
 */
class LuaLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage(): LuaLanguage = LuaLanguage.INSTANCE

    override fun getCodeSample(settingsType: LanguageCodeStyleSettingsProvider.SettingsType): String {
        return CodeStyleAbstractPanel.readFromFile(this.javaClass, "preview.lua.template")
    }

    override fun getIndentOptionsEditor(): IndentOptionsEditor {
        return SmartIndentOptionsEditor()
    }

    override fun getDefaultCommonSettings(): CommonCodeStyleSettings {
        val commonSettings = CommonCodeStyleSettings(language)
        commonSettings.initIndentOptions()
        return commonSettings
    }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: LanguageCodeStyleSettingsProvider.SettingsType) {
        when (settingsType) {
            LanguageCodeStyleSettingsProvider.SettingsType.SPACING_SETTINGS -> {
//                consumer.showCustomOption(LuaCodeStyleSettings::class.java, "SPACE_AFTER_TABLE_FIELD_SEP", "After field sep", SPACES_OTHER)
//                consumer.showCustomOption(LuaCodeStyleSettings::class.java, "SPACE_AROUND_BINARY_OPERATOR", "Around binary operator", SPACES_OTHER)
//                consumer.showCustomOption(LuaCodeStyleSettings::class.java, "SPACE_INSIDE_INLINE_TABLE", "Inside inline table", SPACES_OTHER)
//                consumer.showStandardOptions("SPACE_AROUND_ASSIGNMENT_OPERATORS",
//                        "SPACE_BEFORE_COMMA",
//                        "SPACE_AFTER_COMMA")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "keep_one_space_between_table_and_bracket",
                        "Space between table and bracket",
                        "Table")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "keep_one_space_between_namedef_and_attribute",
                        "Space between namedef and attribute",
                        "Local")
                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "long_chain_expression_allow_one_space_after_colon",
                        "Long chain expression allow one space after colon",
                        "Expression")
                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "table_append_expression_no_space",
                        "Table append expression no space",
                        "Expression")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "label_no_indent",
                        "Label no indent",
                        "Indent")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "do_statement_no_indent",
                        "Do statement no indent",
                        "Indent")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "if_condition_no_continuation_indent",
                        "If condition no continuation_indent",
                        "Indent")
            }

            LanguageCodeStyleSettingsProvider.SettingsType.WRAPPING_AND_BRACES_SETTINGS -> {

                consumer.showCustomOption(
                        LuaCodeStyleSettings::class.java,
                        "call_arg_parentheses",
                        "Call arg parentheses",
                        "Function",
                        CodeStyleSettingOptions.call_arg_parentheses_names,
                        CodeStyleSettingOptions.call_arg_parentheses_values
                )
                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "quote_style",
                        "Quote style",
                        "String",
                        CodeStyleSettingOptions.quote_style_names,
                        CodeStyleSettingOptions.quote_style_values
                )

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "align_call_args",
                        "Align call args",
                        "Function")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "local_assign_continuation_align_to_first_expression",
                        "Assign continuation align to first expression",
                        "Layout")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "align_function_define_params",
                        "Align function define params",
                        "Layout")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "align_table_field_to_first_field",
                        "Align table field to first field",
                        "Layout")


                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "if_condition_align_with_each_other",
                        "If condition align with each other",
                        "Layout")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "continuous_assign_statement_align_to_equal_sign",
                        "Continuous assign statement align to equal sign",
                        "Layout")

                consumer.showCustomOption(LuaCodeStyleSettings::class.java,
                        "continuous_assign_table_field_align_to_equal_sign",
                        "Continuous assign table field align to equal sign",
                        "Layout")

                // line layout
                consumer.showCustomOption(
                        LuaCodeStyleSettings::class.java,
                        "keep_line_after_if_statement",
                        "Keep line after if statement",
                        "Line-layout",
                        CodeStyleSettingOptions.line_layout_names,
                        CodeStyleSettingOptions.line_layout_values,
                )
                consumer.showCustomOption(
                        LuaCodeStyleSettings::class.java,
                        "keep_line_after_do_statement",
                        "Keep line after do statement",
                        "Line-layout",
                        CodeStyleSettingOptions.line_layout_names,
                        CodeStyleSettingOptions.line_layout_values,
                )
                consumer.showCustomOption(
                        LuaCodeStyleSettings::class.java,
                        "keep_line_after_while_statement",
                        "Keep line after while statement",
                        "Line-layout",
                        CodeStyleSettingOptions.line_layout_names,
                        CodeStyleSettingOptions.line_layout_values,
                )
                consumer.showCustomOption(
                        LuaCodeStyleSettings::class.java,
                        "keep_line_after_repeat_statement",
                        "Keep line after repeat statement",
                        "Line-layout",
                        CodeStyleSettingOptions.line_layout_names,
                        CodeStyleSettingOptions.line_layout_values,
                )
                consumer.showCustomOption(
                        LuaCodeStyleSettings::class.java,
                        "keep_line_after_for_statement",
                        "Keep line after for statement",
                        "Line-layout",
                        CodeStyleSettingOptions.line_layout_names,
                        CodeStyleSettingOptions.line_layout_values,
                )
                consumer.showCustomOption(
                        LuaCodeStyleSettings::class.java,
                        "keep_line_after_local_or_assign_statement",
                        "Keep line after local or assign statement",
                        "Line-layout",
                        CodeStyleSettingOptions.line_layout_names,
                        CodeStyleSettingOptions.line_layout_values,
                )

                consumer.showCustomOption(
                        LuaCodeStyleSettings::class.java,
                        "keep_line_after_function_define_statement",
                        "Keep line after function define statement",
                        "Line-layout",
                        CodeStyleSettingOptions.line_layout_names,
                        CodeStyleSettingOptions.line_layout_values,
                )


            }
            LanguageCodeStyleSettingsProvider.SettingsType.BLANK_LINES_SETTINGS -> {

            }
            else -> {
            }
        }
    }
}
