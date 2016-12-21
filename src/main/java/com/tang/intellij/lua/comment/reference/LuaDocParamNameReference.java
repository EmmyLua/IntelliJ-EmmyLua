package com.tang.intellij.lua.comment.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocParamNameRef;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.psi.LuaParamNameDef;
import com.tang.intellij.lua.psi.LuaParametersOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 函数参数引用
 * Created by tangzx on 2016/11/25.
 */
public class LuaDocParamNameReference extends PsiReferenceBase<LuaDocParamNameRef> {
    public LuaDocParamNameReference(@NotNull LuaDocParamNameRef element) {
        super(element);
    }

    @Override
    public TextRange getRangeInElement() {
        return new TextRange(0, myElement.getTextLength());
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(element, resolve());
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement id = LuaElementFactory.createIdentifier(myElement.getProject(), newElementName);
        myElement.getFirstChild().replace(id);
        return id;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        LuaCommentOwner owner = LuaCommentUtil.findOwner(myElement);

        if (owner != null) {
            String name = myElement.getText();
            if (owner instanceof LuaParametersOwner) {
                LuaParametersOwner parametersOwner = (LuaParametersOwner) owner;
                return findParamWithName(parametersOwner.getParamNameDefList(), name);
            }
        }
        return null;
    }

    private PsiElement findParamWithName(List<LuaParamNameDef> defList, String str) {
        for (LuaParamNameDef nameDef : defList) {
            if (nameDef.getText().equals(str)) {
                return nameDef;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
