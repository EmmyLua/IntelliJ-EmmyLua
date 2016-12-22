package com.tang.intellij.lua.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.tang.intellij.lua.psi.LuaCallExpr;
import com.tang.intellij.lua.psi.LuaPsiResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/12/9.
 */
public class LuaRequireReference extends PsiReferenceBase<LuaCallExpr> {

    private String pathString;
    private TextRange range = TextRange.EMPTY_RANGE;

    LuaRequireReference(@NotNull LuaCallExpr callExpr) {
        super(callExpr);

        PsiElement path = callExpr.getFirstStringArg();

        if (path != null) {
            pathString = path.getText();
            pathString = pathString.substring(1, pathString.length() - 1);

            int start = path.getTextOffset() - callExpr.getTextOffset() + 1;
            int end = start + path.getTextLength() - 2;
            range = new TextRange(start, end);
        }
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(element, resolve());
    }

    @Override
    public TextRange getRangeInElement() {
        return range;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return LuaPsiResolveUtil.resolveRequireFile(pathString, myElement.getProject());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
