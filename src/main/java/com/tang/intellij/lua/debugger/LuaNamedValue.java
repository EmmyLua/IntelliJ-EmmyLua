package com.tang.intellij.lua.debugger;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by tangzx on 2017/1/1.
 */
public class LuaNamedValue extends XNamedValue {

    private String desc;
    private IElementType type;

    public LuaNamedValue(String name, String desc, IElementType type) {
        super(name);
        this.desc = desc;
        this.type = type;
    }

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        xValueNode.setPresentation(LuaIcons.LOCAL_VAR, null, desc, type == LuaTypes.TABLE_CONSTRUCTOR);
    }

    public static LuaNamedValue create(LuaTableField info) {
        String name = info.getName();
        assert name != null;
        String text = info.getText();

        VariableVisitor visitor = new VariableVisitor();
        info.acceptChildren(visitor);

        return new LuaNamedValue(name, visitor.objectDesc, visitor.objectType);
    }

    static class VariableVisitor extends LuaVisitor {
        IElementType objectType;
        String objectDesc;
        @Override
        public void visitTableConstructor(@NotNull LuaTableConstructor o) {
            LuaFieldList fieldList = o.getFieldList();
            if (fieldList != null) {
                List<LuaTableField> list = fieldList.getTableFieldList();

                LuaTableField type = list.get(0);
                PsiElement typeFirstChild = type.getFirstChild();
                if (typeFirstChild != null) {
                    PsiElement child = typeFirstChild.getFirstChild();
                    objectType = child.getNode().getElementType();
                }

                LuaTableField last = list.get(1);
                objectDesc = last.getText();
                if (objectType != LuaTypes.STRING) {
                    objectDesc = objectDesc.substring(1, objectDesc.length() - 1);
                }
             }
        }

        @Override
        public void visitPsiElement(@NotNull LuaPsiElement o) {
            o.acceptChildren(this);
        }
    }

    public static LuaNamedValue createEvalValue(String result) {
        //do local _={"{name = \"aaa\"} --[[table: 00D5E990]]"};return _;end
        //do local _={"function() --[[..skipped..]] end --[[function: 00B5CBF0]]"};return _;en
        Pattern pattern = Pattern.compile("\\{\"(.+)--\\[\\[(.+?)\\]\\]\"\\};return _;end");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            String desc = matcher.group(2);
            IElementType type = LuaTypes.TABLE_CONSTRUCTOR;
            if (desc.startsWith("function"))
                type = LuaTypes.LAMBDA_FUNC_DEF;
            return new LuaNamedValue("eval", desc, type);
        }

        //do local _={"123"};return _;end
        pattern = Pattern.compile("\\{\"(.+)\"\\};return _;end");
        matcher = pattern.matcher(result);
        if (matcher.find()) {
            return new LuaNamedValue("eval", matcher.group(1), LuaTypes.STRING);
        }
        return new LuaNamedValue("eval", "nil", LuaTypes.STRING);
    }
}
