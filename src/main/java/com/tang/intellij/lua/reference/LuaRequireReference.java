package com.tang.intellij.lua.reference;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 * Created by tangzx on 2016/12/9.
 */
public class LuaRequireReference extends PsiReferenceBase<LuaCallExpr> {

    private String pathString;
    private TextRange range = TextRange.EMPTY_RANGE;

    LuaRequireReference(@NotNull LuaCallExpr callExpr) {
        super(callExpr);

        LuaArgs args = callExpr.getArgs();
        if (args != null) {
            PsiElement path = null;

            // require "xxx"
            for (PsiElement child = args.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getNode().getElementType() == LuaTypes.STRING) {
                    path = child;
                    break;
                }
            }
            // require("")
            if (path == null) {
                LuaExprList exprList = args.getExprList();
                if (exprList != null) {
                    List<LuaExpr> list = exprList.getExprList();
                    if (list.size() == 1 && list.get(0) instanceof LuaValueExpr) {
                        LuaValueExpr valueExpr = (LuaValueExpr) list.get(0);
                        PsiElement node = valueExpr.getFirstChild();
                        if (node.getNode().getElementType() == LuaTypes.STRING) {
                            path = node;
                        }
                    }
                }
            }

            if (path != null && path.getNode().getElementType() == LuaTypes.STRING) {
                pathString = path.getText();
                pathString = pathString.substring(1, pathString.length() - 1);

                int start = path.getTextOffset() - callExpr.getTextOffset() + 1;
                int end = start + path.getTextLength() - 2;
                range = new TextRange(start, end);
            }
        }
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return myElement.getManager().areElementsEquivalent(element, resolve());
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
