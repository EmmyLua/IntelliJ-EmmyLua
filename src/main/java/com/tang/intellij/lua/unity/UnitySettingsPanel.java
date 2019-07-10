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

package com.tang.intellij.lua.unity;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class UnitySettingsPanel implements SearchableConfigurable, Configurable.NoScroll {
    private JPanel myPanel;
    private JTextField textUnityCompletionServicePort;
    private UnitySettings settings = UnitySettings.getInstance();

    public UnitySettingsPanel() {
        textUnityCompletionServicePort.setDocument(new IntegerDocument());
        textUnityCompletionServicePort.setText(String.valueOf(settings.getPort()));
    }

    @NotNull
    @Override
    public String getId() {
        return "UnitySettingsPanel";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Unity Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return myPanel;
    }

    @Override
    public boolean isModified() {
        return settings.getPort() != getPort();
    }

    @Override
    public void apply() {
        settings.setPort(getPort());
        settings.fireChanged();
    }

    private int getPort() {
        return Integer.parseInt(textUnityCompletionServicePort.getText());
    }

    class IntegerDocument extends PlainDocument {
        public void insertString(int offset, String s, AttributeSet attributeSet) throws BadLocationException {
            try {
                Integer.parseInt(s);
            } catch (Exception ex) {
                return;
            }
            super.insertString(offset, s, attributeSet);
        }
    }
}