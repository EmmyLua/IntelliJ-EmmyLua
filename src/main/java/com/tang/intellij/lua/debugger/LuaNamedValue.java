package com.tang.intellij.lua.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.xdebugger.frame.*;
import com.tang.intellij.lua.debugger.commands.EvaluatorCommand;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by tangzx on 2017/1/1.
 */
public class LuaNamedValue extends XNamedValue {

    private String desc;
    private String type;

    private LuaNamedValue(String name, String desc, String type) {
        super(name);

        if (!Objects.equals(type, "string")) {
            desc = desc.substring(1, desc.length() - 1);
        }
        this.desc = desc;
        this.type = type;
    }

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        xValueNode.setPresentation(LuaIcons.LOCAL_VAR, null, desc, Objects.equals("table", type));
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (Objects.equals("table", type)) {
            String expr = "return " + getName();
            EvaluatorCommand command = new EvaluatorCommand(expr, true, (data)->{
                XValueChildrenList list = createEvalValueList(data);
                if (list == null) list = XValueChildrenList.EMPTY;
                node.addChildren(list, true);
            });
            command.exec();
        } else super.computeChildren(node);
    }

    public static LuaNamedValue createStack(LuaTableField info) {
        String name = info.getName();
        assert name != null;

        VariableVisitor visitor = new VariableVisitor();
        info.acceptChildren(visitor);

        return new LuaNamedValue(name, visitor.objectDesc, visitor.objectType);
    }

    static PsiElement getValueElement(LuaTableField field) {
        PsiElement typeFirstChild = field.getFirstChild();
        if (typeFirstChild != null) {
            return typeFirstChild.getFirstChild();
        }
        return null;
    }

    static class VariableVisitor extends LuaVisitor {
        String objectType = "string";
        String objectDesc = "unknown";

        @Override
        public void visitTableConstructor(@NotNull LuaTableConstructor o) {
            LuaFieldList fieldList = o.getFieldList();
            if (fieldList != null) {
                List<LuaTableField> list = fieldList.getTableFieldList();

                LuaTableField type = list.get(0);
                PsiElement child = getValueElement(type);
                LuaTableField last = list.get(1);
                objectDesc = last.getText();

                if (child != null) {
                    if (objectDesc.contains("table"))
                        objectType = "table";
                    else if (objectDesc.contains("function"))
                        objectType = "function";
                }
             }
        }

        @Override
        public void visitPsiElement(@NotNull LuaPsiElement o) {
            o.acceptChildren(this);
        }
    }

    static LuaNamedValue createEvalValue(String name, String result) {
        //do local _={"{name = \"aaa\"} --[[table: 00D5E990]]"};return _;end
        //do local _={"function() --[[..skipped..]] end --[[function: 00B5CBF0]]"};return _;en
        Pattern pattern = Pattern.compile("\\{\"(.+)--\\[\\[(.+?)\\]\\]\"\\};return _;end");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            String desc = matcher.group(2);
            String type = "table";
            if (desc.startsWith("function"))
                type = "function";
            return new LuaNamedValue(name, desc, type);
        }

        //do local _={"123"};return _;end
        pattern = Pattern.compile("\\{\"(.+)\"\\};return _;end");
        matcher = pattern.matcher(result);
        if (matcher.find()) {
            return new LuaNamedValue(name, matcher.group(1), "string");
        }
        return new LuaNamedValue(name, "nil", "string");
    }

    //{\"table: 00AD00A8\", \"table\"}
    private static LuaNamedValue createEvalValue(LuaTableConstructor tableConstructor) {
        LuaFieldList fieldList = tableConstructor.getFieldList();
        assert fieldList != null;
        List<LuaTableField> list = fieldList.getTableFieldList();
        assert list.size() == 3;
        String name = list.get(0).getText();
        String value = list.get(1).getText();
        String type = list.get(2).getText();

        if (name.startsWith("\"")) name = name.substring(1, name.length() - 1);
        if (type.startsWith("\"")) type = type.substring(1, type.length() - 1);

        return new LuaNamedValue(name, value, type);
    }

    private static XValueChildrenList createEvalValueList(String result) {
        Pattern pattern = Pattern.compile("\\{\"(.+)--\\[\\[(.+?)\\]\\]\"\\};return _;end");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            String tblString = matcher.group(1);
            tblString = tblString.replace("\\\"", "\"");
            Project project = LuaDebugProcess.getCurrent().getSession().getProject();
            LuaFile file = LuaElementFactory.createFile(project, "local a = " + tblString);
            ValueListVisitor visitor = new ValueListVisitor();
            file.acceptChildren(visitor);
            return visitor.list;
        }

        return null;
    }

    private static class ValueListVisitor extends LuaVisitor {

        XValueChildrenList list = new XValueChildrenList();
        int level;

        @Override
        public void visitElement(PsiElement element) {
            element.acceptChildren(this);
        }

        @Override
        public void visitTableConstructor(@NotNull LuaTableConstructor o) {
            level++;
            if (level == 2)
                list.add(createEvalValue(o));
            else
                visitPsiElement(o);
            level--;
        }
    }
}
