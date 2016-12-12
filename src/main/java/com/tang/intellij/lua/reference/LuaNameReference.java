package com.tang.intellij.lua.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.psi.LuaNameRef;
import com.tang.intellij.lua.psi.LuaPsiResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/11/26.
 */
public class LuaNameReference extends PsiReferenceBase<LuaNameRef> {
    public LuaNameReference(LuaNameRef element) {
        super(element);
    }

    @Override
    public TextRange getRangeInElement() {
        return new TextRange(0, myElement.getTextLength());
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement newId = LuaElementFactory.createIdentifier(myElement.getProject(), newElementName);
        myElement.getFirstChild().replace(newId);
        return newId;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return LuaPsiResolveUtil.resolve(myElement);
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(element, resolve());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
