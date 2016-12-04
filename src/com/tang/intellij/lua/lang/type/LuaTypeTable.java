package com.tang.intellij.lua.lang.type;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaField;
import com.tang.intellij.lua.psi.LuaFieldList;
import com.tang.intellij.lua.psi.LuaTableConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Table 类型
 * Created by TangZX on 2016/12/4.
 */
public class LuaTypeTable extends LuaType {

    public static LuaTypeTable create(LuaTableConstructor tableElement) {
        return new LuaTypeTable(tableElement);
    }

    public LuaTableConstructor tableConstructor;
    public List<String> fieldStringList = new ArrayList<>();

    protected LuaTypeTable(LuaTableConstructor tableElement) {
        tableConstructor = tableElement;
        LuaFieldList fieldList = tableElement.getFieldList();
        if (fieldList != null) {
            for (LuaField field : fieldList.getFieldList()) {
                PsiElement id = field.getNameDef();
                if (id != null) {
                    fieldStringList.add(id.getText());
                }
            }
        }
    }

    // table 没有 class method
    @Override
    public void addMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {}

    @Override
    public void addFieldCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {
        for (String s : fieldStringList) {
            LookupElementBuilder elementBuilder = LookupElementBuilder.create(s)
                    .withIcon(AllIcons.Nodes.Field)
                    .withTypeText("Table");

            completionResultSet.addElement(elementBuilder);
        }
    }
}
