package com.tang.intellij.lua.codeInsight

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile
import com.tang.intellij.lua.lang.LuaFileType

/**

 * Created by tangzx on 2017/2/11.
 */
class LuaCodeTemplateContextType : TemplateContextType("LUA_CODE", "Lua") {

    override fun isInContext(psiFile: PsiFile, i: Int): Boolean {
        return psiFile.fileType === LuaFileType.INSTANCE
    }
}
