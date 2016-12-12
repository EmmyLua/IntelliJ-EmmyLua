package com.tang.intellij.lua.psi.stub.elements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.impl.LuaClassMethodDefImpl;
import com.tang.intellij.lua.psi.index.LuaClassMethodIndex;
import com.tang.intellij.lua.psi.stub.LuaClassMethodStub;
import com.tang.intellij.lua.psi.stub.impl.LuaClassMethodStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/4.
 */
public class LuaClassMethodStubElementType extends IStubElementType<LuaClassMethodStub, LuaClassMethodDef> {
    public LuaClassMethodStubElementType() {
        super("LuaClassMethodStubElementType", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaClassMethodDef createPsi(@NotNull LuaClassMethodStub luaClassMethodStub) {
        return new LuaClassMethodDefImpl(luaClassMethodStub, LuaElementType.CLASS_METHOD_DEF);
    }

    @Override
    public LuaClassMethodStub createStub(@NotNull LuaClassMethodDef luaClassMethodFuncDef, StubElement stubElement) {
        String clazzName = resolveClassName(luaClassMethodFuncDef);
        return new LuaClassMethodStubImpl(clazzName, stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class_method";
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        PsiElement psi = node.getPsi();
        return psi instanceof LuaClassMethodDef && resolveClassName((LuaClassMethodDef) psi) != null;
    }

    @Override
    public void serialize(@NotNull LuaClassMethodStub luaClassMethodStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeUTFFast(luaClassMethodStub.getClassName());
    }

    @NotNull
    @Override
    public LuaClassMethodStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        String name = stubInputStream.readUTFFast();

        return new LuaClassMethodStubImpl(name, stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaClassMethodStub luaClassMethodStub, @NotNull IndexSink indexSink) {
        String className = luaClassMethodStub.getClassName();
        if (className != null) {
            indexSink.occurrence(LuaClassMethodIndex.KEY, className);
        }
    }

    static String resolveClassName(LuaClassMethodDef luaClassMethodFuncDef) {
        return luaClassMethodFuncDef.getClassName();
    }
}
