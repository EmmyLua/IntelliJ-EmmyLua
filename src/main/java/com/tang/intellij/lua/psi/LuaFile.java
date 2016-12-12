package com.tang.intellij.lua.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.tang.intellij.lua.lang.LuaFileType;
import com.tang.intellij.lua.lang.LuaLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaFile extends PsiFileBase {
    public LuaFile(@NotNull FileViewProvider fileViewProvider) {
        super(fileViewProvider, LuaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return LuaFileType.INSTANCE;
    }
}
