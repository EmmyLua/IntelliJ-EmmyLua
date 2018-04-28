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

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.openapi.options.Configurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import com.tang.intellij.lua.lang.LuaLanguage

/**

 * Created by tangzx on 2017/2/22.
 */
class LuaCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
    override fun createSettingsPage(settings: CodeStyleSettings, originalSettings: CodeStyleSettings): Configurable {
        return object : CodeStyleAbstractConfigurable(settings, originalSettings, "EmmyLua") {

            override fun getHelpTopic() = "reference.settingsdialog.codestyle.lua"

            override fun createPanel(codeStyleSettings: CodeStyleSettings): CodeStyleAbstractPanel {
                val language = LuaLanguage.INSTANCE
                val currentSettings = currentSettings
                return object : TabbedLanguageCodeStylePanel(language, currentSettings, codeStyleSettings) {
                    override fun initTabs(styleSettings: CodeStyleSettings) {
                        //super.initTabs(styleSettings);
                        addIndentOptionsTab(styleSettings)
                        addSpacesTab(styleSettings)
                        //addBlankLinesTab(styleSettings)
                        addWrappingAndBracesTab(styleSettings)
                    }
                }
            }
        }
    }

    override fun getConfigurableDisplayName() = LuaLanguage.INSTANCE.displayName

    override fun createCustomSettings(settings: CodeStyleSettings?): CustomCodeStyleSettings? {
        return LuaCodeStyleSettings(settings)
    }
}
