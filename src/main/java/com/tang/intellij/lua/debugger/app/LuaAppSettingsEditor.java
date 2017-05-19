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
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.TextFieldCompletionProvider;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.tang.intellij.lua.debugger.DebuggerType;
import com.tang.intellij.lua.lang.LuaFileType;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 * Created by tangzx on 2017/5/7.
 */
public class LuaAppSettingsEditor extends SettingsEditor<LuaAppRunConfiguration> {
    private final DefaultComboBoxModel<DebuggerType> debuggerDataModel;
    private TextFieldWithBrowseButton myProgram;
    private JPanel myPanel;
    private JComboBox<DebuggerType> myDebugger;
    private TextFieldWithCompletion myFile;
    private TextFieldWithBrowseButton myWorkingDir;
    private Project project;

    LuaAppSettingsEditor(Project project) {
        this.project = project;
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
        myProgram.addBrowseFolderListener("Choose Program", "Choose program file", project, descriptor);
        descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        myWorkingDir.addBrowseFolderListener("Choose Working Dir", "Choose working dir", project, descriptor);

        debuggerDataModel = new DefaultComboBoxModel<>(new DebuggerType[] {
                DebuggerType.Attach,
                DebuggerType.Mob
        });
        myDebugger.setModel(debuggerDataModel);
        myDebugger.addItemListener(e -> {
            fireEditorStateChanged();
        });
    }

    @Override
    protected void resetEditorFrom(@NotNull LuaAppRunConfiguration luaAppRunConfiguration) {
        myProgram.setText(luaAppRunConfiguration.getProgram());
        myWorkingDir.setText(luaAppRunConfiguration.getWorkingDir());
        myFile.setText(luaAppRunConfiguration.getFile());
        myDebugger.setSelectedItem(luaAppRunConfiguration.getDebuggerType());
    }

    @Override
    protected void applyEditorTo(@NotNull LuaAppRunConfiguration luaAppRunConfiguration) throws ConfigurationException {
        luaAppRunConfiguration.setProgram(myProgram.getText());
        luaAppRunConfiguration.setWorkingDir(myWorkingDir.getText());
        luaAppRunConfiguration.setFile(myFile.getText());
        luaAppRunConfiguration.setDebuggerType((DebuggerType) myDebugger.getSelectedItem());
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
                    String url = LuaFileUtil.getShortUrl(project, virtualFile);
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
    }
}
