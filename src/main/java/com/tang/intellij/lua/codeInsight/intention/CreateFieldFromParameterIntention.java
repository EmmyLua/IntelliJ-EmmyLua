package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.psi.LuaBlock;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaParamNameDef;
import com.tang.intellij.lua.psi.LuaPsiTreeUtil;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaClassIndex;
import com.tang.intellij.lua.ty.TyClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/2/11.
 */
public class CreateFieldFromParameterIntention extends BaseIntentionAction {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Create field for parameter";
    }

    @NotNull
    @Override
    public String getText() {
        return getFamilyName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        LuaParamNameDef paramNameDef = getLuaParamNameDef(editor, psiFile);
        if (paramNameDef == null)
            return false;
        PsiElement parent = paramNameDef.getParent();
        if (parent == null)
            return false;
        parent = parent.getParent();
        return parent instanceof LuaClassMethodDef;
    }

    private LuaParamNameDef getLuaParamNameDef(Editor editor, PsiFile psiFile) {
        int offset = editor.getCaretModel().getOffset();
        return LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, offset, LuaParamNameDef.class, false);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        LuaParamNameDef paramNameDef = getLuaParamNameDef(editor, psiFile);
        if (paramNameDef != null) {
            LuaClassMethodDef methodDef = PsiTreeUtil.getParentOfType(paramNameDef, LuaClassMethodDef.class);
            if (methodDef != null) {
                LuaBlock block = PsiTreeUtil.getChildOfType(methodDef.getFuncBody(), LuaBlock.class);
                assert block != null;

                ApplicationManager.getApplication().invokeLater(() -> {
                    String paramName = paramNameDef.getText();
                    CreateFieldFromParameterDialog dialog = new CreateFieldFromParameterDialog(project, paramName);
                    if (!dialog.showAndGet()) {
                        return;
                    }

                    String fieldName = dialog.getFieldName();
                    boolean createDoc = dialog.isCreateDoc();
                    if (createDoc) {
                        SearchContext context = new SearchContext(project);
                        TyClass classType = methodDef.getClassType(context);
                        if (classType != null) {
                            LuaDocClassDef def = LuaClassIndex.find(classType.getClassName(), context);
                            if (def != null) {
                                String tempString = String.format("\n---@field public %s $type$$END$", fieldName);
                                TemplateManager templateManager = TemplateManager.getInstance(project);
                                Template template = templateManager.createTemplate("", "", tempString);
                                template.addVariable("type", new MacroCallNode(new SuggestTypeMacro()), new TextExpression("table"), true);
                                template.setToReformat(true);

                                TextRange textRange = def.getTextRange();
                                editor.getCaretModel().moveToOffset(textRange.getEndOffset());
                                templateManager.startTemplate(editor, template, new TemplateEditingAdapter() {
                                    @Override
                                    public void templateFinished(Template template, boolean brokenOff) {
                                        insertFieldAssign(project, editor, block, paramName, fieldName);
                                    }
                                });
                                return;
                            }
                        }
                    }

                    insertFieldAssign(project, editor, block, paramName, fieldName);
                });
            }
        }
    }

    private void insertFieldAssign(@NotNull Project project, Editor editor, LuaBlock block, String paramName, String fieldName) {
        String tempString = String.format("\nself.%s = %s$END$", fieldName, paramName);
        TemplateManager templateManager = TemplateManager.getInstance(project);
        Template template = templateManager.createTemplate("", "", tempString);
        template.setToReformat(true);

        editor.getCaretModel().moveToOffset(block.getTextOffset());
        templateManager.startTemplate(editor, template);
    }
}
