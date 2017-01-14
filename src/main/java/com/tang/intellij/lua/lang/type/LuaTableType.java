package com.tang.intellij.lua.lang.type;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaFieldList;
import com.tang.intellij.lua.psi.LuaTableConstructor;
import com.tang.intellij.lua.psi.LuaTableField;
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

    public LuaTableConstructor tableConstructor;
    private List<String> fieldStringList;

    private LuaTableType(LuaTableConstructor tableElement) {
        super(tableElement);
        tableConstructor = tableElement;
    }

    @Override
    public String getClassNameText() {
        return "table";
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
                                       boolean withSuper) {
        super.addFieldCompletions(completionParameters, completionResultSet, bold, withSuper);
        InitFieldList();
        for (String s : fieldStringList) {
            LookupElementBuilder elementBuilder = LookupElementBuilder.create(s)
                    .withIcon(LuaIcons.CLASS_FIELD)
                    .withTypeText("Table");

            completionResultSet.addElement(elementBuilder);
        }
    }
}
