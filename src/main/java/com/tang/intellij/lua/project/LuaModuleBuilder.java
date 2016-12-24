package com.tang.intellij.lua.project;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * lua ModuleBuilder
 * Created by tangzx on 2016/12/24.
 */
public class LuaModuleBuilder extends ModuleBuilder {
    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        doAddContentEntry(rootModel);
    }

    @Override
    public ModuleType getModuleType() {
        return LuaModuleType.getInstance();
    }
}
