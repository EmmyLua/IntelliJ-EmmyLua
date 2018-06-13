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

package com.tang.intellij.lua.debugger.remote;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.HyperlinkAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

/**
 *
 * Created by tangzx on 2017/5/4.
 */
public class LuaMobSettingsEditor extends SettingsEditor<LuaMobConfiguration> {
    private JTextField port;
    private JPanel myPanel;
    private HoverHyperlinkLabel mobdebugLink;

    @Override
    protected void resetEditorFrom(@NotNull LuaMobConfiguration luaMobConfiguration) {
        port.setText(String.valueOf(luaMobConfiguration.getPort()));
    }

    @Override
    protected void applyEditorTo(@NotNull LuaMobConfiguration luaMobConfiguration) throws ConfigurationException {
        try {
            luaMobConfiguration.setPort(Integer.parseInt(port.getText()));
        } catch (NumberFormatException ignored) {

        }
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        port.addActionListener(e -> LuaMobSettingsEditor.this.fireEditorStateChanged());
        return myPanel;
    }

    private void createUIComponents() {
        mobdebugLink = new HoverHyperlinkLabel("Get mobdebug.lua 0.7+");
        mobdebugLink.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            protected void hyperlinkActivated(HyperlinkEvent hyperlinkEvent) {
                BrowserUtil.browse("https://github.com/pkulchenko/MobDebug/releases");
            }
        });
    }
}
