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

/**
 * LuaCodeStyleSettings
 * Created by tangzx on 2017/2/22.
 */
public class LuaCodeStyleSettings extends CustomCodeStyleSettings {

    public boolean SPACE_AFTER_TABLE_FIELD_SEP = true;

    public boolean ALIGN_TABLE_FIELD_ASSIGN = false;

    LuaCodeStyleSettings(CodeStyleSettings container) {
        super(LuaLanguage.INSTANCE.getID(), container);
    }
}
