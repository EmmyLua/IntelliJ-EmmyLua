package com.tang.intellij.lua.doc.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.tang.intellij.lua.doc.reference.LuaClassNameReference;
import com.tang.intellij.lua.doc.reference.LuaDocParamNameReference;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaDocPsiImplUtil {

    public static PsiReference getReference(LuaDocParamNameRef paramNameRef) {
        return new LuaDocParamNameReference(paramNameRef);
    }

    public static PsiReference getReference(LuaDocClassNameRef docClassNameRef) {
        return new LuaClassNameReference(docClassNameRef);
    }

    public static String getName(LuaDocClassName className) {
        return className.getText();
    }

    public static PsiElement setName(LuaDocClassName className, String newName) {
        return null;
    }
}
