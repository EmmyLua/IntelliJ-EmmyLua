package com.tang.intellij.lua.doc.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.doc.reference.LuaClassNameReference;
import com.tang.intellij.lua.doc.reference.LuaDocParamNameReference;
import com.tang.intellij.lua.psi.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaDocPsiImplUtil {

    @NotNull
    public static PsiReference getReference(LuaDocParamNameRef paramNameRef) {
        return new LuaDocParamNameReference(paramNameRef);
    }

    @NotNull
    public static PsiReference getReference(LuaDocClassNameRef docClassNameRef) {
        return new LuaClassNameReference(docClassNameRef);
    }

    @NotNull
    public static String getName(LuaDocClassName className) {
        return className.getText();
    }

    public static PsiElement setName(LuaDocClassName className, String newName) {
        return null;
    }

    public static LuaDocClassDef resolveType(LuaDocParamDef paramDec) {
        LuaDocClassNameRef ref = paramDec.getClassNameRef();
        if (ref == null) return null;

        Collection<LuaDocClassDef> list = LuaClassIndex.getInstance().get(ref.getText(), paramDec.getProject(), new ProjectAndLibrariesScope(paramDec.getProject()));
        if (list.size() > 0) return list.iterator().next();
        return null;
    }
}
