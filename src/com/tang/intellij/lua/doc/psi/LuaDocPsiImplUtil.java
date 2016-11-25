package com.tang.intellij.lua.doc.psi;

import com.intellij.psi.PsiReference;
import com.tang.intellij.lua.doc.reference.LuaDocParamNameReference;
import com.tang.intellij.lua.psi.LuaCommentOwner;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaDocPsiImplUtil {

    public static LuaCommentOwner getOwner() {
        return null;
    }

    public static PsiReference getReference(LuaDocParamNameRef nameRef) {
        return new LuaDocParamNameReference(nameRef);
    }
}
