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

package com.tang.intellij.lua.lang;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;

import javax.swing.*;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaIcons {
    public static final Icon FILE = IconLoader.getIcon("/icons/lua.png");
    public static final Icon CLASS = AllIcons.Nodes.Class;
    public static final Icon CLASS_FIELD = AllIcons.Nodes.Field;
    public static final Icon CLASS_METHOD = AllIcons.Nodes.Method;
    public static final Icon GLOBAL_FUNCTION = new LayeredIcon(AllIcons.Nodes.Method, AllIcons.Nodes.StaticMark);
    public static final Icon GLOBAL_FIELD = new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.StaticMark);
    public static final Icon LOCAL_VAR = AllIcons.Nodes.Variable;
    public static final Icon LOCAL_FUNCTION = new LayeredIcon(AllIcons.Nodes.Function, AllIcons.Nodes.C_private);
    public static final Icon FUNCTION_PARAMETER = AllIcons.Nodes.Variable;
    public static final Icon WORD = AllIcons.Actions.Edit;

    public static final Icon MODULE = AllIcons.Nodes.Module;
}
