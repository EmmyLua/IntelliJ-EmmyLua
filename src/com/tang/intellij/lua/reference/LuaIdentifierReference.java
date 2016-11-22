package com.tang.intellij.lua.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.psi.LuaIdentifier;
import com.tang.intellij.lua.psi.LuaLocalDef;
import com.tang.intellij.lua.psi.LuaNameList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/11/22.
 */
public class LuaIdentifierReference extends PsiReferenceBase<LuaIdentifier> implements PsiPolyVariantReference {
    
    public LuaIdentifierReference(@NotNull LuaIdentifier element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PsiFile file = getElement().getContainingFile();
        LuaLocalDef def = PsiTreeUtil.findChildOfType(file, LuaLocalDef.class);
        if (def != null) {
            LuaNameList list = def.getNameList();
            PsiElement id = PsiTreeUtil.findChildOfType(list, PsiElement.class);
            return id;
        }
        return null;
    }

    @Override
    public TextRange getRangeInElement() {
        return myElement.getTextRange();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        return new ResolveResult[0];
    }
}
