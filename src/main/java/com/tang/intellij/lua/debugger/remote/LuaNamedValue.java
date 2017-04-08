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

package com.tang.intellij.lua.debugger.remote;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.tang.intellij.lua.debugger.remote.commands.EvaluatorCommand;
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
    private XSourcePosition parentPosition;

    private LuaNamedValue(XSourcePosition psiFile, String name, String desc, String type) {
        super(name);

        if (!Objects.equals(type, "string") && desc.startsWith("\"")) {
            desc = desc.substring(1, desc.length() - 1);
        }
        this.parentPosition = psiFile;
        this.desc = desc;
        this.type = type;
    }

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        xValueNode.setPresentation(LuaIcons.LOCAL_VAR, null, desc, Objects.equals("table", type));
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable xNavigatable) {
        if (parentPosition != null) {
            VirtualFile file = parentPosition.getFile();
            Project project = LuaDebugProcess.getCurrent().getSession().getProject();
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor(file);

            if (psiFile != null && editor instanceof TextEditor) {
                TextEditor textEditor = (TextEditor) editor;
                Document document = textEditor.getEditor().getDocument();
                int lineEndOffset = document.getLineStartOffset(parentPosition.getLine());
                PsiElement element = psiFile.findElementAt(lineEndOffset);
                LuaPsiTreeUtil.walkUpLocalNameDef(element, nameDef -> {
                    if (myName.equals(nameDef.getName())) {
                        XSourcePosition position = XSourcePositionImpl.createByElement(nameDef);
                        xNavigatable.setSourcePosition(position);
                        return false;
                    }
                    return true;
                });
            }
        }
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (Objects.equals("table", type)) {
            String expr = "return " + getName();
            EvaluatorCommand command = new EvaluatorCommand(expr, true, (data)->{
                XValueChildrenList list = createEvalValueList(parentPosition, data);
                if (list == null) list = XValueChildrenList.EMPTY;
                node.addChildren(list, true);
            });
            command.exec();
        } else super.computeChildren(node);
    }

    public static LuaNamedValue createStack(XSourcePosition parentPosition, LuaTableField info) {
        String name = info.getName();
        assert name != null;

        VariableVisitor visitor = new VariableVisitor();
        info.acceptChildren(visitor);

        return new LuaNamedValue(parentPosition, name, visitor.objectDesc, visitor.objectType);
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

    static LuaNamedValue createEvalValue(XSourcePosition parentPosition, String name, String result) {
        Pattern pattern = Pattern.compile("\\{\"(.+)--\\[\\[(.+?)\\]\\]\"\\};return _;end");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            String tblString = matcher.group(1);
            tblString = tblString.replace("\\\"", "\"");
            Project project = LuaDebugProcess.getCurrent().getSession().getProject();
            LuaFile file = LuaElementFactory.createFile(project, "local a = " + tblString);
            LuaTableConstructor tableConstructor = PsiTreeUtil.findChildOfType(file, LuaTableConstructor.class);
            return createEvalValue(parentPosition, tableConstructor);
        }

        //do local _={"123"};return _;end
        /*pattern = Pattern.compile("\\{\"(.+)\"\\};return _;end");
        matcher = pattern.matcher(result);
        if (matcher.find()) {
            return new LuaNamedValue(name, matcher.group(1), "string");
        }*/
        return new LuaNamedValue(parentPosition, name, "nil", "string");
    }

    //{\"table: 00AD00A8\", \"table\"}
    private static LuaNamedValue createEvalValue(XSourcePosition parentPosition, LuaTableConstructor tableConstructor) {
        LuaFieldList fieldList = tableConstructor.getFieldList();
        assert fieldList != null;
        List<LuaTableField> list = fieldList.getTableFieldList();
        assert list.size() == 3;
        String name = list.get(0).getText();
        String value = list.get(1).getText();
        String type = list.get(2).getText();

        if (name.startsWith("\"")) name = name.substring(1, name.length() - 1);
        if (type.startsWith("\"")) type = type.substring(1, type.length() - 1);

        return new LuaNamedValue(parentPosition, name, value, type);
    }

    private static XValueChildrenList createEvalValueList(XSourcePosition psiFile, String result) {
        Pattern pattern = Pattern.compile("\\{\"(.+)--\\[\\[(.+?)\\]\\]\"\\};return _;end");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            String tblString = matcher.group(1);
            tblString = tblString.replace("\\\"", "\"");
            Project project = LuaDebugProcess.getCurrent().getSession().getProject();
            LuaFile file = LuaElementFactory.createFile(project, "local a = " + tblString);
            ValueListVisitor visitor = new ValueListVisitor(psiFile);
            file.acceptChildren(visitor);
            return visitor.list;
        }

        return null;
    }

    private static class ValueListVisitor extends LuaVisitor {

        XValueChildrenList list = new XValueChildrenList();
        int level;
        private XSourcePosition parentPosition;

        ValueListVisitor(XSourcePosition parentPosition) {

            this.parentPosition = parentPosition;
        }

        @Override
        public void visitElement(PsiElement element) {
            element.acceptChildren(this);
        }

        @Override
        public void visitTableConstructor(@NotNull LuaTableConstructor o) {
            level++;
            if (level == 2)
                list.add(createEvalValue(parentPosition, o));
            else
                visitPsiElement(o);
            level--;
        }
    }
}
