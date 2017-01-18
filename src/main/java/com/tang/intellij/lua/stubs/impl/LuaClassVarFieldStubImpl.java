package com.tang.intellij.lua.stubs.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaVarStub;

/**
 *
 * Created by tangzx on 2017/1/12.
 */
public class LuaClassVarFieldStubImpl extends StubBase<LuaVar> implements LuaVarStub {

    private LuaIndexExpr indexExpr;
    private String typeName;
    private String fieldName;
    private boolean isGlobal;
    private boolean isValid;



    public LuaClassVarFieldStubImpl(StubElement parent,
                                    IStubElementType elementType) {
        super(parent, elementType);
        this.isValid = false;
        this.isGlobal = false;
    }

    public LuaClassVarFieldStubImpl(StubElement parent,
                                    IStubElementType elementType,
                                    LuaVar var) {
        super(parent, elementType);
        //this.indexExpr = indexExpr;
        this.isValid = checkValid(var);

        if (this.isValid) {
            if (var.getNameRef() != null) {
                this.isGlobal = true;
                this.fieldName = var.getNameRef().getText();
            } else {
                this.isGlobal = false;
                assert var.getExpr() instanceof LuaIndexExpr;
                LuaIndexExpr indexExpr = (LuaIndexExpr) var.getExpr();
                assert indexExpr.getId() != null;
                this.indexExpr = indexExpr;
            }
        }
    }

    public LuaClassVarFieldStubImpl(StubElement stubElement,
                                    IStubElementType type,
                                    String typeName,
                                    String fieldName) {
        super(stubElement, type);
        this.isGlobal = false;
        this.typeName = typeName;
        this.fieldName = fieldName;
    }

    public LuaClassVarFieldStubImpl(StubElement stubElement,
                                    IStubElementType type,
                                    String fieldName) {
        super(stubElement, type);
        this.isGlobal = true;
        this.fieldName = fieldName;
    }

    public boolean isValid() {
        return isValid;
    }

    private boolean checkValid(LuaVar var) {
        LuaAssignStat assignStat = PsiTreeUtil.getParentOfType(var, LuaAssignStat.class);
        assert assignStat != null;
        if (assignStat.getExprList() == null) // 确定是XXX.XX = XXX 完整形式
            return false;

        LuaExpr expr = var.getExpr();
        //XXX.XXX = ??
        if (expr instanceof LuaIndexExpr) {
            LuaIndexExpr indexExpr = (LuaIndexExpr) expr;
            return indexExpr.getId() != null;
        }
        //XXX = ??
        LuaNameRef nameRef = var.getNameRef();
        return nameRef != null && LuaPsiResolveUtil.resolveLocal(nameRef) == null;
    }

    public String getTypeName() {
        if (typeName == null) {
            SearchContext context = new SearchContext(indexExpr.getProject());
            context.setCurrentStubFile(indexExpr.getContainingFile());

            LuaTypeSet set = indexExpr.guessPrefixType(context);
            if (set != null) {
                LuaType type = set.getFirst();
                if (type != null)
                    typeName = type.getClassNameText();
            }
        }
        return typeName;
    }

    @Override
    public String getFieldName() {
        if (fieldName == null) {
            PsiElement id = indexExpr.getId();
            if (id != null)
                fieldName = id.getText();
        }
        return fieldName;
    }

    @Override
    public boolean isGlobal() {
        return isGlobal;
    }

}
