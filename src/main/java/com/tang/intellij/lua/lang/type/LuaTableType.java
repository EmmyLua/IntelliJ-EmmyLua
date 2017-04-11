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

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.psi.LuaFieldList;
import com.tang.intellij.lua.psi.LuaTableConstructor;
import com.tang.intellij.lua.psi.LuaTableField;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Table 类型
 * Created by TangZX on 2016/12/4.
 */
public class LuaTableType extends LuaType {

    public static LuaTableType create(LuaTableConstructor tableElement) {
        return new LuaTableType(tableElement);
    }

    public static String getTypeName(LuaTableConstructor tableConstructor) {
        String fileName = tableConstructor.getContainingFile().getName();
        return String.format("%s@(%d)table", fileName, tableConstructor.getNode().getStartOffset());
    }

    public LuaTableConstructor tableConstructor;
    private List<String> fieldStringList;

    private LuaTableType(LuaTableConstructor tableElement) {
        tableConstructor = tableElement;
        clazzName = getTypeName(tableElement);
    }

    @Override
    public String getClassName() {
        return clazzName;
    }

    private void InitFieldList() {
        if (fieldStringList == null) {
            fieldStringList = new ArrayList<>();
            LuaFieldList fieldList = tableConstructor.getFieldList();
            if (fieldList != null) {
                for (LuaTableField field : fieldList.getTableFieldList()) {
                    PsiElement id = field.getNameIdentifier();
                    if (id != null) {
                        fieldStringList.add(id.getText());
                    }
                }
            }
        }
    }

    @Override
    protected void addFieldCompletions(@NotNull CompletionParameters completionParameters,
                                       @NotNull CompletionResultSet completionResultSet,
                                       boolean bold,
                                       SearchContext context) {
        super.addFieldCompletions(completionParameters, completionResultSet, bold, context);
        InitFieldList();
        for (String s : fieldStringList) {
            LookupElementBuilder elementBuilder = LookupElementBuilder.create(s)
                    .withIcon(LuaIcons.CLASS_FIELD)
                    .withTypeText(getClassName());

            completionResultSet.addElement(elementBuilder);
        }
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
