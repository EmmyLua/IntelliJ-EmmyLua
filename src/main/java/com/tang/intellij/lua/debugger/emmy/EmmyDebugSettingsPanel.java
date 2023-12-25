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

package com.tang.intellij.lua.debugger.emmy;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfoRt;
import com.tang.intellij.lua.lang.LuaFileType;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.Objects;

public class EmmyDebugSettingsPanel extends SettingsEditor<EmmyDebugConfiguration> implements DocumentListener {
    private JComboBox<EmmyDebugTransportType> typeCombox;
    private JLabel type;
    private JTextField tcpHostInput;
    private JTextField tcpPortInput;
    private JLabel tcpHostLabel;
    private JLabel tcpPortLabel;
    private JTextField pipelineInput;
    private JLabel pipeNameLabel;
    private JPanel panel;
    private JPanel codePanel;
    private JCheckBox waitIDECheckBox;
    private JCheckBox breakWhenIDEConnectedCheckBox;

    private JRadioButton x64RadioButton;
    private JRadioButton x86RadioButton;
    private JPanel winArchPanel;
    private ButtonGroup winArchGroup;

    private EditorEx editorEx;

    public EmmyDebugSettingsPanel(Project project) {
        // type
        DefaultComboBoxModel<EmmyDebugTransportType> model = new DefaultComboBoxModel<>();
        model.addElement(EmmyDebugTransportType.TCP_CLIENT);
        model.addElement(EmmyDebugTransportType.TCP_SERVER);
        /*for (EmmyDebugTransportType value : EmmyDebugTransportType.values()) {
            model.addElement(value);
        }*/
        typeCombox.addActionListener(e -> {
            setType((EmmyDebugTransportType) typeCombox.getSelectedItem());
            onChanged();
        });
        typeCombox.setModel(model);
        // tcp
        tcpHostInput.setText("localhost");
        tcpHostInput.getDocument().addDocumentListener(this);
        tcpPortInput.setText("9966");
        tcpPortInput.setDocument(new IntegerDocument());
        tcpPortInput.getDocument().addDocumentListener(this);
        // pipe
        pipelineInput.setText("emmylua");
        pipelineInput.getDocument().addDocumentListener(this);

        waitIDECheckBox.addActionListener(e -> onChanged());
        breakWhenIDEConnectedCheckBox.addActionListener(e -> onChanged());

        // arch
        winArchGroup = new ButtonGroup();
        winArchPanel.setVisible(SystemInfoRt.isWindows);
        winArchGroup.add(x64RadioButton);
        winArchGroup.add(x86RadioButton);
        x64RadioButton.addChangeListener(e -> onChanged());
        x86RadioButton.addChangeListener(e -> onChanged());

        // editor
        editorEx = createEditorEx(project);
        codePanel.add(editorEx.getComponent(), BorderLayout.CENTER);

        updateCode();
    }

    private void onChanged() {
        if (isClient()) {
            breakWhenIDEConnectedCheckBox.setEnabled(waitIDECheckBox.isSelected());
        } else {
            breakWhenIDEConnectedCheckBox.setEnabled(true);
        }
        fireEditorStateChanged();
        updateCode();
    }

    @Override
    protected void resetEditorFrom(@NotNull EmmyDebugConfiguration configuration) {
        typeCombox.setSelectedItem(configuration.getType());
        setType(configuration.getType());

        tcpHostInput.setText(configuration.getHost());
        tcpPortInput.setText(String.valueOf(configuration.getPort()));

        pipelineInput.setText(configuration.getPipeName());

        if (SystemInfoRt.isWindows) {
            if (configuration.getWinArch() == EmmyWinArch.X64) {
                x64RadioButton.setSelected(true);
            } else {
                x86RadioButton.setSelected(true);
            }
        }
    }

    @Override
    protected void applyEditorTo(@NotNull EmmyDebugConfiguration configuration) {
        EmmyDebugTransportType type = (EmmyDebugTransportType) typeCombox.getSelectedItem();
        assert type != null;
        configuration.setType(type);

        configuration.setHost(tcpHostInput.getText());
        configuration.setPort(Integer.parseInt(tcpPortInput.getText()));

        configuration.setPipeName(pipelineInput.getText());
        if (SystemInfoRt.isWindows) {
            configuration.setWinArch(x64RadioButton.isSelected() ? EmmyWinArch.X64 : EmmyWinArch.X86);
        }
    }

    protected void setType(EmmyDebugTransportType type) {
        boolean isTCP = type == EmmyDebugTransportType.TCP_CLIENT || type == EmmyDebugTransportType.TCP_SERVER;
        tcpHostLabel.setVisible(isTCP);
        tcpPortLabel.setVisible(isTCP);
        tcpHostInput.setVisible(isTCP);
        tcpPortInput.setVisible(isTCP);

        pipeNameLabel.setVisible(!isTCP);
        pipelineInput.setVisible(!isTCP);

        waitIDECheckBox.setVisible(isClient());
    }

    private boolean isClient() {
        EmmyDebugTransportType type = getType();
        return type == EmmyDebugTransportType.TCP_CLIENT || type == EmmyDebugTransportType.PIPE_CLIENT;
    }

    private EmmyDebugTransportType getType() {
        return (EmmyDebugTransportType) typeCombox.getSelectedItem();
    }

    private String getHost() {
        return tcpHostInput.getText();
    }

    private int getPort() {
        int port = 0;
        try {
            port = Integer.parseInt(tcpPortInput.getText());
        } catch (Exception ignored) {
        }
        return port;
    }

    private String getPipeName() {
        return pipelineInput.getText();
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return panel;
    }

    private EditorEx createEditorEx(Project project) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document editorDocument = editorFactory.createDocument("");
        return (EditorEx)editorFactory.createEditor(editorDocument, project, LuaFileType.INSTANCE, false);
    }

    private void updateCode() {
        ApplicationManager.getApplication().runWriteAction(this::updateCodeImpl);
    }

    private String getDebuggerFolder() {
        if (SystemInfoRt.isWindows)
            return LuaFileUtil.INSTANCE.getPluginVirtualFile("debugger/emmy/windows");
        if (SystemInfoRt.isMac)
            return LuaFileUtil.INSTANCE.getPluginVirtualFile("debugger/emmy/mac");
        return LuaFileUtil.INSTANCE.getPluginVirtualFile("debugger/emmy/linux");
    }

    private void updateCodeImpl() {
        StringBuilder sb = new StringBuilder();

        sb.append("local EmmyStringOutputMode = {\n")
                .append("    Auto = 'Auto',\n")
                .append("    Concise = 'Concise',\n")
                .append("    Complete = 'Complete',\n")
                .append("}\n")
                .append("rawset(_G, 'EmmyStringOutputMode', EmmyStringOutputMode.Auto)\n");

        if (SystemInfoRt.isWindows) {
            EmmyWinArch arch = x64RadioButton.isSelected() ? EmmyWinArch.X64 : EmmyWinArch.X86;
            sb.append("package.cpath = package.cpath .. ';")
                    .append(getDebuggerFolder())
                    .append("/")
                    .append(arch.getDesc())
                    .append("/?.dll'\n");
        } else if (SystemInfoRt.isMac) {
            sb.append("package.cpath = package.cpath .. ';")
                    .append(getDebuggerFolder())
                    .append("/")
                    .append(Objects.equals(System.getProperty("os.arch"), "arm64") ? "arm64": "x64")
                    .append("/?.dylib'\n");
        } else {
            sb.append("package.cpath = package.cpath .. ';")
                    .append(getDebuggerFolder())
                    .append("/?.so'\n");
        }
        sb.append("local dbg = require('emmy_core')\n");
        EmmyDebugTransportType type = getType();
        if (type == EmmyDebugTransportType.PIPE_CLIENT) {
            sb.append("dbg.pipeListen('").append(getPipeName()).append("')\n");
        }
        else if (type == EmmyDebugTransportType.PIPE_SERVER) {
            sb.append("dbg.pipeConnect('").append(getPipeName()).append("')\n");
        }
        else if (type == EmmyDebugTransportType.TCP_CLIENT) {
            sb.append("dbg.tcpListen('").append(getHost()).append("', ").append(getPort()).append(")\n");
        }
        else if (type == EmmyDebugTransportType.TCP_SERVER) {
            sb.append("dbg.tcpConnect('").append(getHost()).append("', ").append(getPort()).append(")\n");
        }

        if (isClient()) {
            if (waitIDECheckBox.isSelected()) {
                sb.append("dbg.waitIDE()\n");
                if (breakWhenIDEConnectedCheckBox.isSelected()) {
                    sb.append("dbg.breakHere()\n");
                }
            }
        } else {
            if (breakWhenIDEConnectedCheckBox.isSelected()) {
                sb.append("dbg.breakHere()\n");
            }
        }
        editorEx.getDocument().setText(sb.toString());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        onChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onChanged();
    }

    static class IntegerDocument extends PlainDocument {
        public void insertString(int offset, String s, AttributeSet attributeSet) throws BadLocationException {
            try {
                Integer.parseInt(s);
            } catch (Exception ex) {
                return;
            }
            super.insertString(offset, s, attributeSet);
        }
    }
}