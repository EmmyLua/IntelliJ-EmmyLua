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

package com.tang.intellij.lua.debugger.remote.value;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 *
 * Created by tangzx on 2017/4/16.
 */
public class LuaRTable extends LuaRValue {
    private XValueChildrenList list;
    private String desc;
    private LuaValue data;

    LuaRTable(@NotNull String name) {
        super(name);
    }

    @Override
    protected void parse(LuaValue data, String desc) {
        this.desc = "table";
        this.data = data;
    }

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        xValueNode.setPresentation(AllIcons.Json.Object, "table", desc, true);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (list == null) {
            list = new XValueChildrenList();
            LuaTable table = data.checktable();
            for (LuaValue key : table.keys()) {
                LuaRValue value = LuaRValue.create(key.toString(), table.get(key), null, session);
                list.add(value);
            }
        }
        node.addChildren(list, true);
    }
}
