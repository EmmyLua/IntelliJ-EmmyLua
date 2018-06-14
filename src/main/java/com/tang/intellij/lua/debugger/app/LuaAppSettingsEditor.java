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

package com.tang.intellij.lua.debugger.app;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.TextFieldCompletionProvider;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.tang.intellij.lua.debugger.DebuggerType;
import com.tang.intellij.lua.lang.LuaFileType;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.SortedMap;

/**
 *
 * Created by tangzx on 2017/5/7.
 */
public class LuaAppSettingsEditor extends SettingsEditor<LuaAppRunConfiguration> {
    private TextFieldWithBrowseButton myProgram;
    private JPanel myPanel;
    private ComboBox<DebuggerType> myDebugger;
    private TextFieldWithCompletion myFile;
    private TextFieldWithBrowseButton myWorkingDir;
    private RawCommandLineEditor parameters;
    private HoverHyperlinkLabel mobdebugLink;
    private EnvironmentVariablesTextFieldWithBrowseButton myEnvironments;
    private ComboBox<String> outputCharset;
    private JCheckBox showConsoleWindowCheckBox;
    private Project project;

    LuaAppSettingsEditor(Project project) {
        this.project = project;
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
        myProgram.addBrowseFolderListener("Choose Program", "Choose program file", project, descriptor);
        descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        myWorkingDir.addBrowseFolderListener("Choose Working Dir", "Choose working dir", project, descriptor);

        DebuggerType[] debuggerTypes;
        if (SystemInfoRt.isWindows)
            debuggerTypes = new DebuggerType[] { DebuggerType.Attach, DebuggerType.Mob };
        else
            debuggerTypes = new DebuggerType[] { DebuggerType.Mob };

        DefaultComboBoxModel<DebuggerType> debuggerDataModel = new DefaultComboBoxModel<>(debuggerTypes);
        myDebugger.setModel(debuggerDataModel);
        myDebugger.addItemListener(e -> {
            DebuggerType debuggerType = (DebuggerType) myDebugger.getSelectedItem();
            mobdebugLink.setVisible(debuggerType == DebuggerType.Mob);
            showConsoleWindowCheckBox.setVisible(debuggerType == DebuggerType.Attach);
            fireEditorStateChanged();
        });

        // output charset
        SortedMap<String, Charset> charsetSortedMap = Charset.availableCharsets();
        DefaultComboBoxModel<String> outputCharsetModel = new DefaultComboBoxModel<>(ArrayUtil.toStringArray(charsetSortedMap.keySet()));
        outputCharset.setModel(outputCharsetModel);
        outputCharset.addItemListener(e -> fireEditorStateChanged());
    }

    @Override
    protected void resetEditorFrom(@NotNull LuaAppRunConfiguration luaAppRunConfiguration) {
        myProgram.setText(luaAppRunConfiguration.getProgram());
        myWorkingDir.setText(luaAppRunConfiguration.getWorkingDir());
        myFile.setText(luaAppRunConfiguration.getFile());
        myDebugger.setSelectedItem(luaAppRunConfiguration.getDebuggerType());
        parameters.setText(luaAppRunConfiguration.getParameters());
        myEnvironments.setEnvs(luaAppRunConfiguration.getEnvs());
        mobdebugLink.setVisible(luaAppRunConfiguration.getDebuggerType() == DebuggerType.Mob);
        outputCharset.setSelectedItem(luaAppRunConfiguration.getCharset());
        showConsoleWindowCheckBox.setSelected(luaAppRunConfiguration.getShowConsole());
    }

    @Override
    protected void applyEditorTo(@NotNull LuaAppRunConfiguration luaAppRunConfiguration) {
        luaAppRunConfiguration.setProgram(myProgram.getText());
        luaAppRunConfiguration.setWorkingDir(myWorkingDir.getText());
        luaAppRunConfiguration.setFile(myFile.getText());
        luaAppRunConfiguration.setDebuggerType((DebuggerType) Objects.requireNonNull(myDebugger.getSelectedItem()));
        luaAppRunConfiguration.setParameters(parameters.getText());
        luaAppRunConfiguration.setEnvs(myEnvironments.getEnvs());
        luaAppRunConfiguration.setCharset((String) Objects.requireNonNull(outputCharset.getSelectedItem()));
        luaAppRunConfiguration.setShowConsole(showConsoleWindowCheckBox.isSelected());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    class LuaFileCompletionProvider extends TextFieldCompletionProvider {

        @Override
        protected void addCompletionVariants(@NotNull String text,
                                             int offset,
                                             @NotNull String prefix,
                                             @NotNull CompletionResultSet result) {
            ProjectRootManager.getInstance(project).getFileIndex().iterateContent(virtualFile -> {
                if (!virtualFile.isDirectory() && virtualFile.getFileType() == LuaFileType.INSTANCE) {
                    String url = LuaFileUtil.getShortPath(project, virtualFile);
                    result.addElement(LookupElementBuilder.create(url).withIcon(LuaIcons.FILE));
                }
                return true;
            });
        }
    }

    private void createUIComponents() {
        myFile = new TextFieldWithCompletion(project,
                new LuaFileCompletionProvider(),
                "",
                true,
                true,
                true,
                true);

        mobdebugLink = new HoverHyperlinkLabel("Get mobdebug.lua 0.7+");
        mobdebugLink.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            protected void hyperlinkActivated(HyperlinkEvent hyperlinkEvent) {
                BrowserUtil.browse("https://github.com/pkulchenko/MobDebug/releases");
            }
        });
    }
}
