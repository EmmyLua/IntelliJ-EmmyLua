package com.tang.intellij.lua.codeInsight;

import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.codeInsight.hints.MethodInfo;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.psi.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * Created by TangZX on 2016/12/14.
 */
public class LuaParameterHintsProvider implements InlayParameterHintsProvider {
    @NotNull
    @Override
    public List<InlayInfo> getParameterHints(PsiElement psiElement) {
        List<InlayInfo> list = new ArrayList<>();
        if (psiElement instanceof LuaCallExpr) {
            LuaCallExpr callExpr = (LuaCallExpr) psiElement;
            LuaTypeSet typeSet = callExpr.guessPrefixType();
            String[] parameters = null;
            if (typeSet != null) {
                PsiElement id = callExpr.getId();
                if (id != null) {
                    LuaType type = typeSet.getType(0);
                    LuaClassMethodDef methodDef = LuaClassMethodIndex.findMethodWithName(type.getClassNameText(), id.getText(), callExpr.getProject(), new ProjectAndLibrariesScope(callExpr.getProject()));
                    if (methodDef != null) {
                        parameters = methodDef.getParameters();
                    }
                }
            }

            LuaArgs args = callExpr.getArgs();
            if (args != null && parameters != null) {
                LuaExprList luaExprList = args.getExprList();
                if (luaExprList != null) {
                    List<LuaExpr> exprList = luaExprList.getExprList();
                    for (int i = 0; i < exprList.size() && i < parameters.length; i++) {
                        LuaExpr expr = exprList.get(i);
                        list.add(new InlayInfo(parameters[i], expr.getTextOffset()));
                    }
                }
            }
        }

        return list;
    }

    @Nullable
    @Override
    public MethodInfo getMethodInfo(PsiElement psiElement) {
        if (psiElement instanceof LuaCallExpr) {
            List<String> list = new ArrayList<>();
            return new MethodInfo("MethodInfo", list);
        }
        return null;
    }

    @NotNull
    @Override
    public Set<String> getDefaultBlackList() {
        Set<String> stringSet = new HashSet<>();
        stringSet.add("test");
        return stringSet;
    }

    @Nullable
    @Override
    public Language getBlackListDependencyLanguage() {
        return null;
    }
}
