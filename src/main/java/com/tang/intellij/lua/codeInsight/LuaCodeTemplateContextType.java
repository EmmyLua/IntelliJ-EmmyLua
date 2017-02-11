package com.tang.intellij.lua.codeInsight;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import com.tang.intellij.lua.lang.LuaFileType;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/2/11.
 */
public class LuaCodeTemplateContextType extends TemplateContextType {
    protected LuaCodeTemplateContextType() {
        super("LUA_CODE", "Lua");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile psiFile, int i) {
        return psiFile.getFileType() == LuaFileType.INSTANCE;
    }
}
