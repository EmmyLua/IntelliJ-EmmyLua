package com.tang.intellij.lua.project;

import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by tangzx on 2016/12/24.
 */
public class LuaModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {
    @Override
    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState moduleConfigurationState) {
        List<ModuleConfigurationEditor> editors = new ArrayList<>();
        DefaultModuleConfigurationEditorFactory factory = DefaultModuleConfigurationEditorFactory.getInstance();

        editors.add(factory.createModuleContentRootsEditor(moduleConfigurationState));
        editors.add(factory.createClasspathEditor(moduleConfigurationState));

        return editors.toArray(new ModuleConfigurationEditor[editors.size()]);
    }
}
