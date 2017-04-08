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

package com.tang.intellij.lua.codeInsight.template.macro;

import com.intellij.codeInsight.template.*;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2017/4/8.
 */
public class SuggestFirstLuaVarNameMacro extends Macro {
    @Override
    public String getName() {
        return "SuggestFirstLuaVarName";
    }

    @Override
    public String getPresentableName() {
        return "SuggestFirstLuaVarName()";
    }

    @Nullable
    @Override
    public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext expressionContext) {
        PsiElement psiElement = expressionContext.getPsiElementAtStartOffset();
        Ref<Result> ref = Ref.create();
        LuaPsiTreeUtil.walkUpLocalNameDef(psiElement, luaNameDef -> {
            ref.set(new TextResult(luaNameDef.getName()));
            return false;
        });
        return ref.get();
    }
}
