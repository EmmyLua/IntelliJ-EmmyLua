package com.tang.intellij.lua.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.psi.impl.LuaGlobalFuncDefImpl;
import com.tang.intellij.lua.stubs.LuaGlobalFuncStub;
import com.tang.intellij.lua.stubs.impl.LuaGlobalFuncStubImpl;
import com.tang.intellij.lua.stubs.index.LuaGlobalFuncIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/11/26.
 */
public class LuaGlobalFuncDefStubElementType extends IStubElementType<LuaGlobalFuncStub, LuaGlobalFuncDef> {

    public LuaGlobalFuncDefStubElementType() {
        super("GLOBAL_FUNC_DEF", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaGlobalFuncDef createPsi(@NotNull LuaGlobalFuncStub luaGlobalFuncStub) {
        return new LuaGlobalFuncDefImpl(luaGlobalFuncStub, LuaElementType.GLOBAL_FUNC_DEF);
    }

    @NotNull
    @Override
    public LuaGlobalFuncStub createStub(@NotNull LuaGlobalFuncDef globalFuncDef, StubElement stubElement) {
        PsiElement nameRef = globalFuncDef.getNameIdentifier();
        assert nameRef != null;
        return new LuaGlobalFuncStubImpl(nameRef.getText(), stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.global_func_def";
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        PsiElement element = node.getPsi();
        if (element instanceof LuaGlobalFuncDef) {
            LuaGlobalFuncDef globalFuncDef = (LuaGlobalFuncDef) element;
            return globalFuncDef.getNameIdentifier() != null;
        }
        return false;
    }

    @Override
    public void serialize(@NotNull LuaGlobalFuncStub luaGlobalFuncStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeUTF(luaGlobalFuncStub.getName());
    }

    @NotNull
    @Override
    public LuaGlobalFuncStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        return new LuaGlobalFuncStubImpl(stubInputStream.readUTF(), stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaGlobalFuncStub luaGlobalFuncStub, @NotNull IndexSink indexSink) {
        indexSink.occurrence(LuaGlobalFuncIndex.KEY, luaGlobalFuncStub.getName());
    }
}
