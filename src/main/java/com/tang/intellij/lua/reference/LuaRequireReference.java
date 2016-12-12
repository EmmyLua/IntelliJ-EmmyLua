package com.tang.intellij.lua.reference;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.tang.intellij.lua.psi.LuaCallExpr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/12/9.
 */
public class LuaRequireReference extends PsiReferenceBase<LuaCallExpr> {

    private String pathString;
    private TextRange range;

    public LuaRequireReference(@NotNull LuaCallExpr element, TextRange range,  String pathString) {
        super(element);
        this.range = range;
        this.pathString = pathString;
    }

    @Override
    public TextRange getRangeInElement() {
        return range;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        int lastDot = pathString.lastIndexOf('.');
        String packagePath = "";
        String fileName = pathString;
        if (lastDot != -1) {
            fileName = pathString.substring(lastDot + 1);
            packagePath = pathString.substring(0, lastDot);
        }
        fileName += ".lua";

        Project project = myElement.getProject();
        PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(packagePath);
        if (psiPackage != null) {
            PsiDirectory[] directories = psiPackage.getDirectories();
            for (PsiDirectory directory : directories) {
                PsiFile file = directory.findFile(fileName);
                if (file != null)
                    return file;
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
