package com.tang.intellij.lua.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaExpr;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaVar;
import com.tang.intellij.lua.psi.impl.LuaVarImpl;
import com.tang.intellij.lua.stubs.LuaVarStub;
import com.tang.intellij.lua.stubs.impl.LuaClassVarFieldStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
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
        LuaExpr expr = psi.getExpr();
        if (expr instanceof LuaIndexExpr) {
            LuaIndexExpr indexExpr = (LuaIndexExpr) expr;
            return indexExpr.getId() != null;
        }
        return false;
    }

    @NotNull
    @Override
    public LuaVarStub createStub(@NotNull LuaVar var, StubElement stubElement) {
        assert var.getExpr() instanceof LuaIndexExpr;
        LuaIndexExpr indexExpr = (LuaIndexExpr) var.getExpr();
        assert indexExpr.getId() != null;

        return new LuaClassVarFieldStubImpl(stubElement, this, indexExpr);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.index_field";
    }

    @Override
    public void serialize(@NotNull LuaVarStub varStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        String text = varStub.getTypeName();
        stubOutputStream.writeBoolean(text != null);
        if (text != null) {
            stubOutputStream.writeUTFFast(text);
        }
    }

    @NotNull
    @Override
    public LuaVarStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        boolean hasType = stubInputStream.readBoolean();
        String text = null;
        if (hasType) text = stubInputStream.readUTFFast();
        return new LuaClassVarFieldStubImpl(stubElement, this, text);
    }

    @Override
    public void indexStub(@NotNull LuaVarStub varStub, @NotNull IndexSink indexSink) {
        String typeName = varStub.getTypeName();
        String fieldName = varStub.getFieldName();
        if (typeName != null && fieldName != null) {
            indexSink.occurrence(LuaClassFieldIndex.KEY, typeName);
            indexSink.occurrence(LuaClassFieldIndex.KEY, typeName + "." + fieldName);
        }
    }
}
