package com.tang.intellij.lua.reference;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaCallExpr;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.psi.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/12/14.
 */
public class LuaCallExprReference extends PsiReferenceBase<LuaCallExpr> {
    private LuaCallExpr expr;

    LuaCallExprReference(LuaCallExpr callExpr) {
        super(callExpr);
        expr = callExpr;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement newId = LuaElementFactory.createIdentifier(myElement.getProject(), newElementName);
        PsiElement oldId = expr.getId();
        assert oldId != null;
        oldId.replace(newId);
        return newId;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(element, resolve());
    }

    @Override
    public TextRange getRangeInElement() {
        PsiElement id = expr.getId();
        assert id != null;
        return new TextRange(id.getStartOffsetInParent(), id.getStartOffsetInParent() + id.getTextLength());
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PsiElement id = expr.getId();
        if (id != null) {
            LuaTypeSet typeSet = expr.guessPrefixType();
            if (typeSet != null) {
                String methodName = id.getText();
                Project project = expr.getProject();
                GlobalSearchScope scope = new ProjectAndLibrariesScope(project);
                for (LuaType luaType : typeSet.getTypes()) {
                    LuaClassMethodDef def = LuaClassMethodIndex.findMethodWithName(luaType.getClassNameText(), methodName, project, scope);
                    if (def != null) {
                        return def.getClassMethodName().getNameDef();
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
