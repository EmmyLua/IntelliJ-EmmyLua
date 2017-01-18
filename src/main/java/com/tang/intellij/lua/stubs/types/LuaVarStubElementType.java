package com.tang.intellij.lua.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaVar;
import com.tang.intellij.lua.psi.LuaVarList;
import com.tang.intellij.lua.psi.impl.LuaVarImpl;
import com.tang.intellij.lua.stubs.LuaVarStub;
import com.tang.intellij.lua.stubs.impl.LuaClassVarFieldStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaGlobalVarIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2017/1/12.
 */
public class LuaVarStubElementType extends IStubElementType<LuaVarStub, LuaVar> {
    public LuaVarStubElementType() {
        super("Var", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaVar createPsi(@NotNull LuaVarStub luaVarStub) {
        return new LuaVarImpl(luaVarStub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        LuaVar psi = (LuaVar) node.getPsi();
        return psi.getParent() instanceof LuaVarList;
    }

    @NotNull
    @Override
    public LuaVarStub createStub(@NotNull LuaVar var, StubElement stubElement) {
        return new LuaClassVarFieldStubImpl(stubElement, this, var);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.index_field";
    }

    @Override
    public void serialize(@NotNull LuaVarStub varStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeBoolean(varStub.isValid());
        if (varStub.isValid()) {
            stubOutputStream.writeBoolean(varStub.isGlobal());
            stubOutputStream.writeUTFFast(varStub.getFieldName());
            if (!varStub.isGlobal()) {
                String text = varStub.getTypeName();
                stubOutputStream.writeBoolean(text != null);
                if (text != null) {
                    stubOutputStream.writeUTFFast(text);
                }
            }
        }
    }

    @NotNull
    @Override
    public LuaVarStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        boolean isValid = stubInputStream.readBoolean();
        if (isValid) {
            boolean isGlobal = stubInputStream.readBoolean();
            String fieldName = stubInputStream.readUTFFast();
            if (isGlobal) {
                return new LuaClassVarFieldStubImpl(stubElement, this, fieldName);
            } else {
                boolean hasType = stubInputStream.readBoolean();
                String text = null;
                if (hasType) text = stubInputStream.readUTFFast();
                return new LuaClassVarFieldStubImpl(stubElement, this, text, fieldName);
            }
        } else {
            return new LuaClassVarFieldStubImpl(stubElement, this);
        }
    }

    @Override
    public void indexStub(@NotNull LuaVarStub varStub, @NotNull IndexSink indexSink) {
        if (varStub.isValid()) {
            String fieldName = varStub.getFieldName();
            if (varStub.isGlobal()) {
                indexSink.occurrence(LuaGlobalVarIndex.KEY, fieldName);
            } else {
                String typeName = varStub.getTypeName();
                if (typeName != null && fieldName != null) {
                    indexSink.occurrence(LuaClassFieldIndex.KEY, typeName);
                    indexSink.occurrence(LuaClassFieldIndex.KEY, typeName + "." + fieldName);
                }
            }
        }
    }
}
