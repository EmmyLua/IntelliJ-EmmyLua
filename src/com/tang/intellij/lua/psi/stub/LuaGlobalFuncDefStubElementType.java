package com.tang.intellij.lua.psi.stub;

import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.psi.LuaTypes;
import com.tang.intellij.lua.psi.impl.LuaGlobalFuncDefImpl;
import com.tang.intellij.lua.psi.index.TypeNameIndex;
import com.tang.intellij.lua.psi.stub.impl.LuaGlobalFuncDefStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by TangZX on 2016/11/22.
 */
public class LuaGlobalFuncDefStubElementType extends IStubElementType<LuaGlobalFuncDefStub, LuaGlobalFuncDef> {

    public LuaGlobalFuncDefStubElementType() {
        super("GLOBAL_FUNC_DEF", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaGlobalFuncDef createPsi(@NotNull LuaGlobalFuncDefStub globalFuncDefStub) {
        return new LuaGlobalFuncDefImpl(globalFuncDefStub, (IStubElementType) LuaTypes.GLOBAL_FUNC_DEF);
    }

    @Override
    public LuaGlobalFuncDefStub createStub(@NotNull LuaGlobalFuncDef def, StubElement stubElement) {
        return new LuaGlobalFuncDefStubImpl(stubElement, (IStubElementType) def);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.global.func";
    }

    @Override
    public void serialize(@NotNull LuaGlobalFuncDefStub globalFuncDefStub, @NotNull StubOutputStream stubOutputStream) throws IOException {

    }

    @NotNull
    @Override
    public LuaGlobalFuncDefStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        return new LuaGlobalFuncDefStubImpl(stubElement, (IStubElementType) LuaTypes.GLOBAL_FUNC_DEF);
    }

    @Override
    public void indexStub(@NotNull LuaGlobalFuncDefStub globalFuncDefStub, @NotNull IndexSink indexSink) {
        System.out.println(globalFuncDefStub.getPsi().getFuncName().getText());
        //indexSink.occurrence(TypeNameIndex.KEY, globalFuncDefStub.getPsi().getFuncName().getText());
    }
}
