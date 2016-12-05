package com.tang.intellij.lua.psi.stub.elements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaClassMethodFuncDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.LuaNameDef;
import com.tang.intellij.lua.psi.LuaNameRef;
import com.tang.intellij.lua.psi.impl.LuaClassMethodFuncDefImpl;
import com.tang.intellij.lua.psi.index.LuaClassMethodIndex;
import com.tang.intellij.lua.psi.stub.LuaClassMethodStub;
import com.tang.intellij.lua.psi.stub.impl.LuaClassMethodStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/4.
 */
public class LuaClassMethodStubElementType extends IStubElementType<LuaClassMethodStub, LuaClassMethodFuncDef> {
    public LuaClassMethodStubElementType() {
        super("LuaClassMethodStubElementType", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaClassMethodFuncDef createPsi(@NotNull LuaClassMethodStub luaClassMethodStub) {
        return new LuaClassMethodFuncDefImpl(luaClassMethodStub, LuaElementType.CLASS_METHOD_DEF);
    }

    @Override
    public LuaClassMethodStub createStub(@NotNull LuaClassMethodFuncDef luaClassMethodFuncDef, StubElement stubElement) {
        LuaNameRef ref = luaClassMethodFuncDef.getClassMethodName().getNameRef();
        String clazzName = null;
        if (ref != null) {
            PsiElement target = ref.resolve();
            if (target instanceof LuaNameDef) {
                LuaNameDef def = (LuaNameDef) target;
                LuaTypeSet typeSet = def.resolveType();
                if (typeSet != null && !typeSet.isEmpty()) {
                    clazzName = typeSet.getType(0).getClassNameText();
                }
            }
        }
        return new LuaClassMethodStubImpl(clazzName, stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class_method";
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return true;
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
}
