package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.macro.SuggestVariableNameMacro;
import com.tang.intellij.lua.psi.LuaFuncBody;
import com.tang.intellij.lua.psi.LuaParamNameDef;

import java.util.List;

public class ArgsInsertHandler implements InsertHandler<LookupElement> {

    private LuaFuncBody funcBody;

    public ArgsInsertHandler(LuaFuncBody def) {
        funcBody = def;
    }

    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        if (funcBody != null) {
            TemplateManager manager = TemplateManager.getInstance(insertionContext.getProject());
            Template template = manager.createTemplate("", "");
            template.addTextSegment("(");

            boolean isFirst = true;
            MacroCallNode name = new MacroCallNode(new SuggestVariableNameMacro());

            List<LuaParamNameDef> paramNameDefList = funcBody.getParamNameDefList();
            for (LuaParamNameDef paramNameDef : paramNameDefList) {
                if (!isFirst)
                    template.addTextSegment(", ");
                template.addVariable(paramNameDef.getName(), name, new TextExpression(paramNameDef.getName()), false);
                isFirst = false;
            }
            template.addTextSegment(")");

            insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getSelectionEndOffset());
            manager.startTemplate(insertionContext.getEditor(), template);
        }
    }
}
