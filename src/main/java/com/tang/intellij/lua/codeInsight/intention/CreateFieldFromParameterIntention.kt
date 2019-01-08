package com.tang.intellij.lua.codeInsight.intention

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.MacroCallNode
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.tang.intellij.lua.codeInsight.template.macro.SuggestTypeMacro
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.search.LuaShortNamesManager
import com.tang.intellij.lua.search.SearchContext
import org.jetbrains.annotations.Nls

/**
 *
 * Created by tangzx on 2017/2/11.
 */
class CreateFieldFromParameterIntention : BaseIntentionAction() {
    @Nls
    override fun getFamilyName(): String {
        return "Create field for parameter"
    }

    override fun getText(): String {
        return familyName
    }

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val paramNameDef = getLuaParamNameDef(editor, psiFile) ?: return false
        var parent: PsiElement? = paramNameDef.parent ?: return false
        parent = parent!!.parent
        return parent is LuaClassMethodDef
    }

    private fun getLuaParamNameDef(editor: Editor, psiFile: PsiFile): LuaParamNameDef? {
        val offset = editor.caretModel.offset
        return LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, offset, LuaParamNameDef::class.java, false)
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val paramNameDef = getLuaParamNameDef(editor, psiFile)
        if (paramNameDef != null) {
            val methodDef = PsiTreeUtil.getParentOfType(paramNameDef, LuaClassMethodDef::class.java)
            if (methodDef != null) {
                val block = PsiTreeUtil.getChildOfType(methodDef.funcBody, LuaBlock::class.java)!!

                ApplicationManager.getApplication().invokeLater {
                    val paramName = paramNameDef.text
                    val dialog = CreateFieldFromParameterDialog(project, paramName)
                    if (!dialog.showAndGet()) {
                        return@invokeLater
                    }

                    val fieldName = dialog.fieldName
                    val createDoc = dialog.isCreateDoc
                    if (createDoc) {
                        val context = SearchContext(project)
                        val classType = methodDef.guessClassType(context)
                        if (classType != null) {
                            val def = LuaShortNamesManager.getInstance(project).findClass(classType.className, context)
                            if (def != null) {
                                val tempString = String.format("\n---@field public %s \$type$\$END$", fieldName)
                                val templateManager = TemplateManager.getInstance(project)
                                val template = templateManager.createTemplate("", "", tempString)
                                template.addVariable("type", MacroCallNode(SuggestTypeMacro()), TextExpression("table"), true)
                                template.isToReformat = true

                                val textRange = def.textRange
                                editor.caretModel.moveToOffset(textRange.endOffset)
                                templateManager.startTemplate(editor, template, object : TemplateEditingAdapter() {
                                    override fun templateFinished(template: Template, brokenOff: Boolean) {
                                        insertFieldAssign(project, editor, block, paramName, fieldName)
                                    }
                                })
                                return@invokeLater
                            }
                        }
                    }

                    insertFieldAssign(project, editor, block, paramName, fieldName)
                }
            }
        }
    }

    private fun insertFieldAssign(project: Project, editor: Editor, block: LuaBlock?, paramName: String, fieldName: String) {
        val tempString = String.format("\nself.%s = %s\$END$", fieldName, paramName)
        val templateManager = TemplateManager.getInstance(project)
        val template = templateManager.createTemplate("", "", tempString)
        template.isToReformat = true

        editor.caretModel.moveToOffset(block!!.textOffset)
        templateManager.startTemplate(editor, template)
    }
}
