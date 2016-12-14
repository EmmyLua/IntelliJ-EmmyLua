package com.tang.intellij.lua.stubs.types;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaFile;
import com.tang.intellij.lua.psi.LuaTypes;
import com.tang.intellij.lua.stubs.LuaFileStub;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/11/27.
 */
public class LuaFileStubElementType extends IStubFileElementType<LuaFileStub> {
    public LuaFileStubElementType() {
        super(LuaLanguage.INSTANCE);
    }

    @Override
    public StubBuilder getBuilder() {
        return new DefaultStubBuilder(){
            @NotNull
            @Override
            protected StubElement createStubForFile(@NotNull PsiFile file) {
                if (file instanceof LuaFile)
                    return new LuaFileStub((LuaFile) file);
                return super.createStubForFile(file);
            }

            @Override
            protected boolean skipChildProcessingWhenBuildingStubs(@NotNull PsiElement parent, @NotNull PsiElement element) {
                IElementType type = element.getNode().getElementType();
                if (type == LuaTypes.BLOCK) {
                    return true;
                }
                return false;
            }
        };
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.file";
    }
}
