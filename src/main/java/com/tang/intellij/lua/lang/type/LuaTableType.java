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

package com.tang.intellij.lua.lang.type;

import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.psi.LuaTableExpr;
import com.tang.intellij.lua.psi.LuaTableField;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Table 类型
 * Created by TangZX on 2016/12/4.
 */
public class LuaTableType extends LuaType {

    public static LuaTableType create(LuaTableExpr tableElement) {
        return new LuaTableType(tableElement);
    }

    public static String getTypeName(LuaTableExpr tableConstructor) {
        String fileName = tableConstructor.getContainingFile().getName();
        return String.format("%s@(%d)table", fileName, tableConstructor.getNode().getStartOffset());
    }

    public LuaTableExpr tableConstructor;
    private List<LuaTableField> tableFields;

    private LuaTableType(LuaTableExpr tableElement) {
        tableConstructor = tableElement;
        clazzName = getTypeName(tableElement);
        isAnonymous = true;
    }

    @Override
    public String getClassName() {
        return clazzName;
    }

    @Override
    public String getDisplayName() {
        return "table";
    }

    private void InitFieldList() {
        if (tableFields == null) {
            tableFields =  tableConstructor.getTableFieldList();
        }
    }

    @Override
    public void processFields(@NotNull SearchContext context,
                              Processor<LuaClassField> processor) {
        InitFieldList();
        for (LuaTableField field : tableFields) {
            processor.process(this, field);
        }
        super.processFields(context, processor);
    }

    @Override
    public LuaClassField findField(String fieldName, SearchContext context) {
        LuaClassField field = super.findField(fieldName, context);
        if (field == null) {
            field = tableConstructor.findField(fieldName);
        }
        return field;
    }
}
