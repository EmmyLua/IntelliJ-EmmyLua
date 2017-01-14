package com.tang.intellij.lua.codeInsight;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 * Created by tangzx on 2016/12/25.
 */
public class LuaParameterInfoHandler implements ParameterInfoHandler<LuaArgs, LuaFuncBodyOwner> {
    @Override
    public boolean couldShowInLookup() {
        return false;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement lookupElement, ParameterInfoContext parameterInfoContext) {
        return new Object[0];
    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(LuaFuncBodyOwner o, ParameterInfoContext parameterInfoContext) {
        return new Object[0];
    }

    @Nullable
    @Override
    public LuaArgs findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        PsiFile file = context.getFile();
        LuaArgs luaArgs = PsiTreeUtil.findElementOfClassAtOffset(file, context.getOffset(), LuaArgs.class, false);
        if (luaArgs != null) {
            LuaCallExpr callExpr = (LuaCallExpr) luaArgs.getParent();
            context.setItemsToShow(new Object[] { callExpr.resolveFuncBodyOwner(new SearchContext(context.getProject())) });
        }
        return luaArgs;
    }

    @Override
    public void showParameterInfo(@NotNull LuaArgs args, @NotNull CreateParameterInfoContext context) {
        context.showHint(args, args.getTextRange().getStartOffset() + 1, this);
    }

    @Nullable
    @Override
    public LuaArgs findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        PsiFile file = context.getFile();
        return PsiTreeUtil.findElementOfClassAtOffset(file, context.getOffset(), LuaArgs.class, false);
    }

    @Override
    public void updateParameterInfo(@NotNull LuaArgs args, @NotNull UpdateParameterInfoContext context) {
        LuaExprList exprList = args.getExprList();
        if (exprList != null) {
            int index = ParameterInfoUtils.getCurrentParameterIndex(exprList.getNode(), context.getOffset(), LuaTypes.COMMA);
            context.setCurrentParameter(index);
        }
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return ",()";
    }

    @Override
    public boolean tracksParameterIndex() {
        return true;
    }

    @Override
    public void updateUI(LuaFuncBodyOwner o, @NotNull ParameterInfoUIContext context) {
        if (o == null)
            return;
        List<LuaParamNameDef> defList = o.getParamNameDefList();
        if (defList != null) {
            StringBuilder sb = new StringBuilder();
            int index = context.getCurrentParameterIndex();
            int start = 0, end = 0;

            for (int i = 0; i < defList.size(); i++) {
                LuaParamNameDef nameDef = defList.get(i);
                if (i > 0)
                    sb.append(", ");
                if (i == index)
                    start = sb.length();
                sb.append(nameDef.getName());

                LuaTypeSet typeSet = nameDef.guessType(new SearchContext(o.getProject()));
                if (typeSet != null) {
                    sb.append(" : ");
                    List<LuaType> types = typeSet.getTypes();
                    for (int j = 0; j < types.size(); j++) {
                        LuaType type = types.get(j);
                        if (type.getClassNameText() != null) {
                            if (j > 0)
                                sb.append(" | ");
                            sb.append(type.getClassNameText());
                        }
                    }
                }

                if (i == index)
                    end = sb.length();
            }

            context.setupUIComponentPresentation(
                    sb.toString(),
                    start,
                    end,
                    false,
                    false,
                    false,
                    context.getDefaultParameterColor()
            );
        }
    }
}
