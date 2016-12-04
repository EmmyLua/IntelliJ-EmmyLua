package com.tang.intellij.lua.lang.type;

import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaField;
import com.tang.intellij.lua.psi.LuaFieldList;
import com.tang.intellij.lua.psi.LuaTableConstructor;

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

    public List<String> fieldStringList = new ArrayList<>();

    protected LuaTypeTable(LuaTableConstructor tableElement) {
        LuaFieldList fieldList = tableElement.getFieldList();
        if (fieldList != null) {
            for (LuaField field : fieldList.getFieldList()) {
                PsiElement id = field.getId();
                if (id != null) {
                    fieldStringList.add(id.getText());
                }
            }
        }
    }

}
