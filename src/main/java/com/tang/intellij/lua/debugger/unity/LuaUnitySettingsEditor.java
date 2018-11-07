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

package com.tang.intellij.lua.debugger.unity;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.tang.intellij.lua.debugger.remote.LuaUnityConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
/**
 *
 * Created by Taigacon on 2018/11/6.
 */
public class LuaUnitySettingsEditor extends SettingsEditor<LuaUnityConfiguration> {
    private JTextField preferedUnityInstanceName;
    private JPanel myPanel;

    @Override
    protected void resetEditorFrom(@NotNull LuaUnityConfiguration luaUnityConfiguration) {
        preferedUnityInstanceName.setText(String.valueOf(luaUnityConfiguration.getPreferedUnityInstanceName()));
    }

    @Override
    protected void applyEditorTo(@NotNull LuaUnityConfiguration luaUnityConfiguration) throws ConfigurationException {
        luaUnityConfiguration.setPreferedUnityInstanceName(preferedUnityInstanceName.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        preferedUnityInstanceName.addActionListener(e -> LuaUnitySettingsEditor.this.fireEditorStateChanged());
        return myPanel;
    }
}
