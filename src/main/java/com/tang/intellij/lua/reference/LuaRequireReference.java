package com.tang.intellij.lua.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.tang.intellij.lua.lang.type.LuaString;
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

        if (path != null && path.getTextLength() > 2) {
            LuaString luaString = LuaString.getContent(path.getText());
            pathString = luaString.value;

            int start = path.getTextOffset() - callExpr.getTextOffset() + luaString.start;
            int end = start + pathString.length();
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
