package com.tang.intellij.lua.psi;

import com.intellij.ide.navigationToolbar.NavBarUpdateQueue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.reference.LuaIdentifierReference;

import java.util.Optional;

/**
 * LuaPsiImplUtil
 * Created by TangZX on 2016/11/22.
 */
public class LuaPsiImplUtil {

    public enum ArgType {
        NAME_LIST,
        TABLE,
        STRING
    }

    public static ArgType getArgType(LuaArgs args) {
        return ArgType.NAME_LIST;
    }

    public static PsiElement setName(LuaIdentifier identifier, String name) {
        return null;
    }

    public static String getName(LuaIdentifier identifier) {
        return identifier.getId().getText();
    }

    public static PsiReference getReference(LuaIdentifier identifier) {
        return new LuaIdentifierReference(identifier);
    }

    public static Optional<PsiElement> resolve(LuaIdentifier identifier) {
        PsiFile file = identifier.getContainingFile();
        LuaLocalDef def = PsiTreeUtil.findChildOfType(file, LuaLocalDef.class);
        if (def != null) {
            return Optional.of((PsiElement) def);
        }
        return Optional.empty();
    }
}
