package com.tang.intellij.lua.codeInsight

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiUtilCore
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaTypes

/**

 * Created by tangzx on 2017/2/11.
 */
class LuaCodeTemplateContextType : TemplateContextType("LUA_CODE", "Lua") {

    override fun isInContext(file: PsiFile, offset: Int): Boolean {
        if (PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(LuaLanguage.INSTANCE)) {
            val element = file.findElementAt(offset)
            if (element == null || element is PsiWhiteSpace || element is PsiComment) {
                return false
            }
            if (element.node.elementType in arrayOf(LuaTypes.STRING, LuaTypes.NUMBER)) {
                return false
            }
        }
        return file.fileType === LuaFileType.INSTANCE
    }
}
