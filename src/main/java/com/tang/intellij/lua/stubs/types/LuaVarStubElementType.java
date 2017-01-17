package com.tang.intellij.lua.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.*;
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
        if (psi.getParent() instanceof LuaVarList) {
            LuaAssignStat assignStat = PsiTreeUtil.getParentOfType(psi, LuaAssignStat.class);
            assert assignStat != null;
            if (assignStat.getExprList() == null) // 确定是XXX.XX = XXX 完整形式
                return false;

            LuaExpr expr = psi.getExpr();
            //XXX.XXX = ??
            if (expr instanceof LuaIndexExpr) {
                LuaIndexExpr indexExpr = (LuaIndexExpr) expr;
                return indexExpr.getId() != null;
            }
            //XXX = ??
            LuaNameRef nameRef = psi.getNameRef();
            if (nameRef != null && LuaPsiResolveUtil.resolveLocal(nameRef) == null) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public LuaVarStub createStub(@NotNull LuaVar var, StubElement stubElement) {
        if (var.getNameRef() != null)
            return new LuaClassVarFieldStubImpl(stubElement, this, var.getNameRef().getText());

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

    @NotNull
    @Override
    public LuaVarStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        boolean isGlobal = stubInputStream.readBoolean();
        String fieldName = stubInputStream.readUTFFast();
        if (isGlobal) {
            return new LuaClassVarFieldStubImpl(stubElement, this, fieldName);
        } else {
            boolean hasType = stubInputStream.readBoolean();
            String text = null;
            if (hasType) text = stubInputStream.readUTFFast();
            return new LuaClassVarFieldStubImpl(stubElement, this, text);
        }
    }

    @Override
    public void indexStub(@NotNull LuaVarStub varStub, @NotNull IndexSink indexSink) {
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
