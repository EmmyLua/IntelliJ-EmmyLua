package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.editor.Editor;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaFuncBody;
import com.tang.intellij.lua.psi.LuaParamNameDef;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/16.
 */
public class CreateDocForMethodIntention extends ClassMethodBasedIntention {
    @Override
    protected boolean isAvailable(LuaClassMethodDef methodDef, Editor editor) {
        return methodDef.getComment() == null || methodDef.getFuncBody() == null;
    }

    @NotNull
    @Override
    public String getText() {
        return "Create Doc";
    }

    @Override
    protected void invoke(LuaClassMethodDef methodDef, Editor editor) {
        LuaFuncBody funcBody = methodDef.getFuncBody();
        if (funcBody != null) {
            TemplateManager templateManager = TemplateManager.getInstance(methodDef.getProject());
            Template template = templateManager.createTemplate("", "");
            template.addTextSegment("---" + methodDef.getMethodName());
            MacroCallNode typeSuggest = new MacroCallNode(new SuggestTypeMacro());

            // params
            List<LuaParamNameDef> parDefList = funcBody.getParamNameDefList();
            for (LuaParamNameDef parDef : parDefList) {
                template.addTextSegment(String.format("\n---@param %s ", parDef.getName()));
                template.addVariable(parDef.getName(), typeSuggest, new TextExpression("table"), false);
            }
            //return
            /*template.addTextSegment("\n---@return ");
            template.addVariable("returnType", typeSuggest, new TextExpression("table"), false);*/

            template.addEndVariable();
            template.addTextSegment("\n");

            editor.getCaretModel().moveToOffset(methodDef.getTextOffset());
            templateManager.startTemplate(editor, template);
        }
    }
}
