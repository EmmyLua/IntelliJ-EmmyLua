package com.tang.intellij.lua.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaPsiResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/12/4.
 */
public class LuaIndexReference extends PsiReferenceBase<LuaIndexExpr> {

    private PsiElement id;

    LuaIndexReference(@NotNull LuaIndexExpr element, PsiElement id) {
        super(element);
        this.id = id;
    }

    @Override
    public TextRange getRangeInElement() {
        int start = id.getTextOffset() - myElement.getTextOffset();
        return new TextRange(start, start + id.getTextLength());
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement newId = LuaElementFactory.createIdentifier(myElement.getProject(), newElementName);
        id.replace(newId);
        return newId;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(resolve(), element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return LuaPsiResolveUtil.resolve(myElement);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
