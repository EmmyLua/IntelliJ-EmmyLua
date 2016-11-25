package com.tang.intellij.lua.doc.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.tang.intellij.lua.doc.psi.LuaDocParamNameRef;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.psi.*;
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
        return true;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        LuaComment comment = null;
        PsiElement searchDoc = myElement;
        while (searchDoc != null && !(searchDoc instanceof PsiFile)) {
            if (searchDoc instanceof LuaComment) {
                comment = (LuaComment) searchDoc;
                break;
            }
            searchDoc = searchDoc.getParent();
        }

        if (comment != null) {
            PsiElement sib = comment.getNextSibling();
            while (!(sib instanceof LuaPsiElement))
                sib = sib.getNextSibling();

            if (sib instanceof LuaLocalFuncDef) {
                String name = myElement.getText();

                LuaLocalFuncDef funcDef = (LuaLocalFuncDef) sib;
                LuaParList list = funcDef.getFuncBody().getParList();
                List<LuaNameDef> defList = list.getNameList().getNameDefList();
                for (LuaNameDef nameDef : defList) {
                    if (nameDef.getId().getText().equals(name)) {
                        return nameDef;
                    }
                }
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
