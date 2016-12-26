package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.macro.SuggestVariableNameMacro;
import com.tang.intellij.lua.psi.LuaParamNameDef;

import java.util.List;

public abstract class ArgsInsertHandler implements InsertHandler<LookupElement> {

    protected abstract List<LuaParamNameDef> getParams();

    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        List<LuaParamNameDef> paramNameDefList = getParams();
        if (paramNameDefList != null) {
            TemplateManager manager = TemplateManager.getInstance(insertionContext.getProject());
            Template template = createTemplate(manager, paramNameDefList);
            insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getSelectionEndOffset());
            manager.startTemplate(insertionContext.getEditor(), template);
        }
    }

    protected Template createTemplate(TemplateManager manager, List<LuaParamNameDef> paramNameDefList) {
        Template template = manager.createTemplate("", "");
        template.addTextSegment("(");

        boolean isFirst = true;
        MacroCallNode name = new MacroCallNode(new SuggestVariableNameMacro());

        for (LuaParamNameDef paramNameDef : paramNameDefList) {
            if (!isFirst)
                template.addTextSegment(", ");
            template.addVariable(paramNameDef.getName(), name, new TextExpression(paramNameDef.getName()), false);
            isFirst = false;
        }
        template.addTextSegment(")");
        return template;
    }
}