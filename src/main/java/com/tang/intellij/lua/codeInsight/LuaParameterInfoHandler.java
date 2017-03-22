/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.codeInsight;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            LuaFuncBodyOwner bodyOwner = callExpr.resolveFuncBodyOwner(new SearchContext(context.getProject()));
            if (bodyOwner != null) {
                LuaParamInfo[] params = bodyOwner.getParams();
                if (params.length == 0)
                    return null;
                context.setItemsToShow(new Object[]{bodyOwner});
            }
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
        LuaParamInfo[] paramInfos = o.getParams();
        if (paramInfos.length > 0) {
            StringBuilder sb = new StringBuilder();
            int index = context.getCurrentParameterIndex();
            int start = 0, end = 0;

            for (int i = 0; i < paramInfos.length; i++) {
                LuaParamInfo paramInfo = paramInfos[i];
                if (i > 0)
                    sb.append(", ");
                if (i == index)
                    start = sb.length();
                sb.append(paramInfo.getName());

                String[] types = paramInfo.getTypes();
                if (types != null && types.length > 0) {
                    sb.append(" : ");
                    for (int j = 0; j < types.length; j++) {
                        if (j > 0) sb.append("|");
                        sb.append(types[j]);
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
