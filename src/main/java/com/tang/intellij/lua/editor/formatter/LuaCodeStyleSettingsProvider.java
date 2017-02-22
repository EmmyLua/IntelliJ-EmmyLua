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

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.tang.intellij.lua.lang.LuaFileType;
import com.tang.intellij.lua.lang.LuaLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 * Created by tangzx on 2017/2/22.
 */
public class LuaCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @NotNull
    @Override
    public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
        return new CodeStyleAbstractConfigurable(settings, originalSettings, "Lua") {

            @Nullable
            @Override
            public String getHelpTopic() {
                return "reference.settingsdialog.codestyle.lua";
            }

            @Override
            protected CodeStyleAbstractPanel createPanel(CodeStyleSettings codeStyleSettings) {
                return new CodeStyleAbstractPanel(codeStyleSettings) {

                    @Override
                    protected int getRightMargin() {
                        return 0;
                    }

                    @Nullable
                    @Override
                    protected EditorHighlighter createHighlighter(EditorColorsScheme editorColorsScheme) {
                        return null;
                    }

                    @NotNull
                    @Override
                    protected FileType getFileType() {
                        return LuaFileType.INSTANCE;
                    }

                    @Nullable
                    @Override
                    protected String getPreviewText() {
                        return null;
                    }

                    @Override
                    public void apply(CodeStyleSettings codeStyleSettings) throws ConfigurationException {

                    }

                    @Override
                    public boolean isModified(CodeStyleSettings codeStyleSettings) {
                        return false;
                    }

                    @Nullable
                    @Override
                    public JComponent getPanel() {
                        return null;
                    }

                    @Override
                    protected void resetImpl(CodeStyleSettings codeStyleSettings) {

                    }
                };
            }
        };
    }

    @Nullable
    @Override
    public String getConfigurableDisplayName() {
        return LuaLanguage.INSTANCE.getID();
    }

    @Nullable
    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
        return new LuaCodeStyleSettings(settings);
    }
}
