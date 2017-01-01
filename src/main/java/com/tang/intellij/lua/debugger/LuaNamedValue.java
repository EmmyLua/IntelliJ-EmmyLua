package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * Created by tangzx on 2017/1/1.
 */
public class LuaNamedValue extends XNamedValue {

    private String desc;

    public LuaNamedValue(String name, String desc) {
        super(name);
        this.desc = desc;
    }

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        xValueNode.setPresentation(LuaIcons.LOCAL_VAR, null, desc, false);
    }

    public static LuaNamedValue create(LuaTableField info) {
        String name = info.getName();
        assert name != null;
        String text = info.getText();

        VariableVisitor visitor = new VariableVisitor();
        info.acceptChildren(visitor);

        return new LuaNamedValue(name, visitor.desc);
    }

    static class VariableVisitor extends LuaVisitor {
        String desc;
        @Override
        public void visitTableConstructor(@NotNull LuaTableConstructor o) {
            LuaFieldList fieldList = o.getFieldList();
            if (fieldList != null) {
                List<LuaTableField> list = fieldList.getTableFieldList();
                LuaTableField last = list.get(list.size() - 1);
                desc = last.getText();
            }
        }

        @Override
        public void visitPsiElement(@NotNull LuaPsiElement o) {
            o.acceptChildren(this);
        }
    }
}
