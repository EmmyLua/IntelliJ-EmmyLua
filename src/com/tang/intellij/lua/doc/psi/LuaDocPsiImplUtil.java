package com.tang.intellij.lua.doc.psi;

import com.intellij.psi.PsiReference;
import com.tang.intellij.lua.doc.reference.LuaDocParamNameReference;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaDocPsiImplUtil {

    public static PsiReference getReference(LuaDocParamNameRef paramNameRef) {
        return new LuaDocParamNameReference(paramNameRef);
    }
}
