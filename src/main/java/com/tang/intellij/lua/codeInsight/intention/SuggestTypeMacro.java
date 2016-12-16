package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.tang.intellij.lua.editor.completion.LuaLookupElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * SuggestTypeMacro
 * Created by TangZX on 2016/12/16.
 */
public class SuggestTypeMacro extends Macro {
    @Override
    public String getName() {
        return "SuggestTypeMacro";
    }

    @Override
    public String getPresentableName() {
        return "SuggestTypeMacro";
    }

    @Nullable
    @Override
    public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext expressionContext) {
        return null;
    }

    @Nullable
    @Override
    public LookupElement[] calculateLookupItems(@NotNull Expression[] params, ExpressionContext context) {
        List<LookupElement> list = new ArrayList<>();
        LuaLookupElement.fillTypes(context.getProject(), list);
        return list.toArray(new LookupElement[list.size()]);
    }
}
