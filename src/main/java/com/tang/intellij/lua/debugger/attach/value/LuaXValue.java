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

package com.tang.intellij.lua.debugger.attach.value;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XNavigatable;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcess;
import com.tang.intellij.lua.psi.LuaPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 *
 * Created by tangzx on 2017/4/2.
 */
public abstract class LuaXValue extends XValue {
    protected LuaAttachDebugProcess process;
    protected String name;
    protected LuaXValue parent;

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {

    }

    public void doParse(Node node) {

    }

    public String toKeyString() {
        return toString();
    }

    public static LuaXValue parse(String data, LuaAttachDebugProcess process) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(data.getBytes()));
            Element root = document.getDocumentElement();
            return parse(root, process);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LuaXValue parse(Node node, LuaAttachDebugProcess process) {
        String nodeName = node.getNodeName();
        LuaXValue value = null;
        switch (nodeName) {
            case "userdata":
                value = new LuaXUserdata();
                break;
            case "table":
                value = new LuaXTable();
                break;
            case "value":
                value = new LuaXPrimitive();
                break;
            case "function":
                value = new LuaXFunction();
                break;
        }
        if (value != null) {
            value.setProcess(process);
            value.doParse(node);
        }
        return value;
    }

    public void setProcess(LuaAttachDebugProcess process) {
        this.process = process;
    }

    public void setName(String v) {
        this.name = v;
    }

    public String getName() {
        return name;
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable xNavigatable) {
        if (name != null && process != null) {
            computeSourcePosition(xNavigatable, name, process.getSession());
        }
    }

    public static void computeSourcePosition(@NotNull XNavigatable xNavigatable, @NotNull String name, @NotNull XDebugSession session) {
        XSourcePosition currentPosition = session.getCurrentPosition();
        if (currentPosition != null) {
            VirtualFile file = currentPosition.getFile();
            Project project = session.getProject();
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor(file);

            if (psiFile != null && editor instanceof TextEditor) {
                TextEditor textEditor = (TextEditor) editor;
                com.intellij.openapi.editor.Document document = textEditor.getEditor().getDocument();
                int lineEndOffset = document.getLineStartOffset(currentPosition.getLine());
                PsiElement element = psiFile.findElementAt(lineEndOffset);
                LuaPsiTreeUtil.walkUpLocalNameDef(element, nameDef -> {
                    if (name.equals(nameDef.getName())) {
                        XSourcePosition position = XSourcePositionImpl.createByElement(nameDef);
                        xNavigatable.setSourcePosition(position);
                        return false;
                    }
                    return true;
                });
            }
        }
    }

    public LuaXValue getParent() {
        return parent;
    }

    public void setParent(LuaXValue parent) {
        this.parent = parent;
    }
}
