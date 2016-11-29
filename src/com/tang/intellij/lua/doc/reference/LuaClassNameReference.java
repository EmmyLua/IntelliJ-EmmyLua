package com.tang.intellij.lua.doc.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.doc.psi.LuaDocClassNameRef;
import com.tang.intellij.lua.psi.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 *
 * Created by TangZX on 2016/11/29.
 */
public class LuaClassNameReference extends PsiReferenceBase<LuaDocClassNameRef> {
    public LuaClassNameReference(@NotNull LuaDocClassNameRef element) {
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
        String name = myElement.getText();
        Collection<LuaDocClassDef> defs = LuaClassIndex.getInstance().get(name, myElement.getProject(), new ProjectAndLibrariesScope(myElement.getProject()));
        if (defs.size() > 0) {
            LuaDocClassDef def = defs.iterator().next();
            return def.getClassName();
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
