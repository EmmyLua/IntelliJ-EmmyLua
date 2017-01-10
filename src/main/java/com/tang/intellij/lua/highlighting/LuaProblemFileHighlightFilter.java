package com.tang.intellij.lua.highlighting;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.tang.intellij.lua.lang.LuaFileType;

/**
 *
 * Created by tangzx on 2017/1/11.
 */
public class LuaProblemFileHighlightFilter implements Condition<VirtualFile> {
    @Override
    public boolean value(VirtualFile file) {
        return file.getFileType() == LuaFileType.INSTANCE;
    }
}
