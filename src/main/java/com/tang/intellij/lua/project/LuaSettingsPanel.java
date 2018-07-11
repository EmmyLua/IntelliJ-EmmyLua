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

package com.tang.intellij.lua.project;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.FileContentUtil;
import com.tang.intellij.lua.lang.LuaLanguageLevel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.SortedMap;

/**
 *
 * Created by Administrator on 2017/6/12.
 */
public class LuaSettingsPanel implements SearchableConfigurable, Configurable.NoScroll {
    private LuaSettings settings;
    private JPanel myPanel;
    private JTextField constructorNames;
    private JCheckBox strictDoc;
    private JCheckBox smartCloseEnd;
    private JCheckBox showWordsInFile;
    private JCheckBox enforceTypeSafety;
    private JCheckBox nilStrict;
    private JCheckBox recognizeGlobalNameAsCheckBox;
    private LuaAdditionalSourcesRootPanel additionalRoots;
    private JCheckBox enableGenericCheckBox;
    private JCheckBox captureOutputDebugString;
    private JCheckBox captureStd;
    private JComboBox<String> defaultCharset;
    private JComboBox<LuaLanguageLevel> languageLevel;

    public LuaSettingsPanel(LuaSettings settings) {
        this.settings = settings;
        constructorNames.setText(settings.getConstructorNamesString());
        strictDoc.setSelected(settings.isStrictDoc());
        smartCloseEnd.setSelected(settings.isSmartCloseEnd());
        showWordsInFile.setSelected(settings.isShowWordsInFile());
        enforceTypeSafety.setSelected(settings.isEnforceTypeSafety());
        nilStrict.setSelected(settings.isNilStrict());
        recognizeGlobalNameAsCheckBox.setSelected(settings.isRecognizeGlobalNameAsType());
        additionalRoots.setRoots(settings.getAdditionalSourcesRoot());
        enableGenericCheckBox.setSelected(settings.getEnableGeneric());

        captureStd.setSelected(settings.getAttachDebugCaptureStd());
        captureOutputDebugString.setSelected(settings.getAttachDebugCaptureOutput());

        SortedMap<String, Charset> charsetSortedMap = Charset.availableCharsets();
        ComboBoxModel<String> outputCharsetModel = new DefaultComboBoxModel<>(ArrayUtil.toStringArray(charsetSortedMap.keySet()));
        defaultCharset.setModel(outputCharsetModel);
        defaultCharset.setSelectedItem(settings.getAttachDebugDefaultCharsetName());

        //language level
        ComboBoxModel<LuaLanguageLevel> lanLevelModel = new DefaultComboBoxModel<>(LuaLanguageLevel.values());
        languageLevel.setModel(lanLevelModel);
        lanLevelModel.setSelectedItem(settings.getLanguageLevel());
    }

    @NotNull
    @Override
    public String getId() {
        return "Lua";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Lua";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return myPanel;
    }

    @Override
    public boolean isModified() {
        return !StringUtil.equals(settings.getConstructorNamesString(), constructorNames.getText()) ||
                settings.isStrictDoc() != strictDoc.isSelected() ||
                settings.isSmartCloseEnd() != smartCloseEnd.isSelected() ||
                settings.isShowWordsInFile() != showWordsInFile.isSelected() ||
                settings.isEnforceTypeSafety() != enforceTypeSafety.isSelected() ||
                settings.isNilStrict() != nilStrict.isSelected() ||
                settings.isRecognizeGlobalNameAsType() != recognizeGlobalNameAsCheckBox.isSelected() ||
                settings.getEnableGeneric() != enableGenericCheckBox.isSelected() ||
                settings.getAttachDebugCaptureOutput() != captureOutputDebugString.isSelected() ||
                settings.getAttachDebugCaptureStd() != captureStd.isSelected() ||
                settings.getAttachDebugDefaultCharsetName() != defaultCharset.getSelectedItem() ||
                settings.getLanguageLevel() != languageLevel.getSelectedItem() ||
                !ArrayUtil.equals(settings.getAdditionalSourcesRoot(), additionalRoots.getRoots(), String::compareTo);
    }

    @Override
    public void apply() {
        settings.setConstructorNamesString(constructorNames.getText());
        constructorNames.setText(settings.getConstructorNamesString());
        settings.setStrictDoc(strictDoc.isSelected());
        settings.setSmartCloseEnd(smartCloseEnd.isSelected());
        settings.setShowWordsInFile(showWordsInFile.isSelected());
        settings.setEnforceTypeSafety(enforceTypeSafety.isSelected());
        settings.setNilStrict(nilStrict.isSelected());
        settings.setRecognizeGlobalNameAsType(recognizeGlobalNameAsCheckBox.isSelected());
        settings.setAdditionalSourcesRoot(additionalRoots.getRoots());
        settings.setEnableGeneric(enableGenericCheckBox.isSelected());
        settings.setAttachDebugCaptureOutput(captureOutputDebugString.isSelected());
        settings.setAttachDebugCaptureStd(captureStd.isSelected());
        settings.setAttachDebugDefaultCharsetName((String)Objects.requireNonNull(defaultCharset.getSelectedItem()));
        LuaLanguageLevel selectedLevel = (LuaLanguageLevel) Objects.requireNonNull(languageLevel.getSelectedItem());
        if (selectedLevel != settings.getLanguageLevel()) {
            settings.setLanguageLevel(selectedLevel);
            StdLibraryProvider.Companion.reload();

            FileContentUtil.reparseOpenedFiles();
        } else {
            for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                DaemonCodeAnalyzer.getInstance(project).restart();
            }
        }
    }
}