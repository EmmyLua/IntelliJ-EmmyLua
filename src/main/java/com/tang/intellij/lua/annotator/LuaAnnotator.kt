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

package com.tang.intellij.lua.annotator

import com.intellij.lang.annotation.AnnotationBuilder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.psi.*
import com.tang.intellij.lua.highlighting.LuaHighlightingData
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext

/**
 * LuaAnnotator
 * Created by TangZX on 2016/11/22.
 */
class LuaAnnotator : Annotator {
    private var myHolder: AnnotationHolder? = null
    private val luaVisitor = LuaElementVisitor()
    private val docVisitor = LuaDocElementVisitor()
    private var isModuleFile: Boolean = false

    companion object {
        private val STD_MARKER = Key.create<Boolean>("lua.std.marker")
        private val UPVALUE = HighlightSeverity("UPVALUE", HighlightSeverity.INFORMATION.myVal + 1)
    }

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        myHolder = annotationHolder
        if (psiElement is LuaDocPsiElement) {
            psiElement.accept(docVisitor)
        } else if (psiElement is LuaPsiElement) {
            val psiFile = psiElement.containingFile
            isModuleFile = if (psiFile is LuaPsiFile) { psiFile.moduleName != null } else false
            psiElement.accept(luaVisitor)
        }
        myHolder = null
    }

    private inline fun newAnnotation(severity: HighlightSeverity, psi: PsiElement, msg: String?, action: (builder: AnnotationBuilder) -> Unit) {
        val builder = if (msg == null) myHolder?.newSilentAnnotation(severity) else myHolder?.newAnnotation(severity, msg)
        builder?.apply {
            range(psi)
            action(this)
            create()
        }
    }

    private inline fun newInfoAnnotation(psi: PsiElement, msg: String?, action: (builder: AnnotationBuilder) -> Unit) {
        newAnnotation(HighlightSeverity.INFORMATION, psi, msg, action)
    }

    internal inner class LuaElementVisitor : LuaVisitor() {

        override fun visitExprStat(o: LuaExprStat) {
            if (o.expr !is LuaCallExpr) {
                if (o.containingFile !is LuaExprCodeFragment) {
                    newAnnotation(HighlightSeverity.ERROR, o, "syntax error") {}
                }
            } else super.visitExprStat(o)
        }

        override fun visitLocalFuncDef(o: LuaLocalFuncDef) {
            val name = o.nameIdentifier

            if (name != null) {
                newInfoAnnotation(name, "Local function \"${o.name}\"") {
                    it.textAttributes(LuaHighlightingData.LOCAL_FUNCTION)
                }
            }
        }

        override fun visitLocalDef(o: LuaLocalDef) {
            val nameList = o.nameList
            if (nameList != null) {
                var child: PsiElement? = nameList.firstChild
                while (child != null) {
                    if (child is LuaNameDef) {
                        newInfoAnnotation(child, "Local variable \"${child.name}\"") {
                            it.textAttributes(LuaHighlightingData.LOCAL_VAR)
                        }
                    }
                    child = child.nextSibling
                }
            }
            super.visitLocalDef(o)
        }

        override fun visitTableField(o: LuaTableField) {
            super.visitTableField(o)
            val id = o.id
            if (id != null) {
                newInfoAnnotation(id, null) {
                    it.textAttributes(LuaHighlightingData.FIELD)
                }
            }
        }

        override fun visitFuncDef(o: LuaFuncDef) {
            val name = o.nameIdentifier
            if (name != null && o.forwardDeclaration == null) {
                if (isModuleFile) {
                    newInfoAnnotation(name, "Module function \"${o.name}\"") {
                        it.textAttributes(LuaHighlightingData.INSTANCE_METHOD)
                    }
                } else {
                    newInfoAnnotation(name, "Global function \"${o.name}\"") {
                        it.textAttributes(LuaHighlightingData.GLOBAL_FUNCTION)
                    }
                }
            }
        }

        override fun visitClassMethodName(o: LuaClassMethodName) {
            val id = o.id ?: return
            newInfoAnnotation(id, null) {
                if (o.dot != null) {
                    it.textAttributes(LuaHighlightingData.STATIC_METHOD)
                } else {
                    it.textAttributes(LuaHighlightingData.INSTANCE_METHOD)
                }
            }
        }

        override fun visitParamNameDef(o: LuaParamNameDef) {
            newInfoAnnotation(o, "Parameter : \"${o.name}\"") {
                it.textAttributes(LuaHighlightingData.PARAMETER)
            }
        }

        override fun visitNameExpr(o: LuaNameExpr) {
            val id = o.firstChild

            val res = resolve(o, SearchContext.get(o.project))
            if (res != null) { //std api highlighting
                val containingFile = res.containingFile
                if (LuaFileUtil.isStdLibFile(containingFile.virtualFile, o.project)) {
                    newInfoAnnotation(o, "Std apis") {
                        it.textAttributes(LuaHighlightingData.STD_API)
                    }
                    o.putUserData(STD_MARKER, true)
                    return
                }
            }

            if (res is LuaParamNameDef) {
                newInfoAnnotation(o, "Parameter : \"${res.name}\"") {
                    it.textAttributes(LuaHighlightingData.PARAMETER)
                }
                checkUpValue(o)
            } else if (res is LuaFuncDef) {
                val resolvedFile = res.containingFile
                if (resolvedFile !is LuaPsiFile || resolvedFile.moduleName == null) {
                    newInfoAnnotation(o, "Global function : \"${res.name}\"") {
                        it.textAttributes(LuaHighlightingData.GLOBAL_FUNCTION)
                    }
                } else {
                    newInfoAnnotation(o, "Module function : \"${res.name}\"") {}
                }
            } else {
                if (id.textMatches(Constants.WORD_SELF)) {
                    newInfoAnnotation(o, null) {
                        it.textAttributes(LuaHighlightingData.SELF)
                    }
                    checkUpValue(o)
                } else if (res is LuaNameDef) {
                    newInfoAnnotation(o, "Local variable \"${o.name}\"") {
                        it.textAttributes(LuaHighlightingData.LOCAL_VAR)
                    }
                    checkUpValue(o)
                } else if (res is LuaLocalFuncDef) {
                    newInfoAnnotation(o, "Local function \"${o.name}\"") {
                        it.textAttributes(LuaHighlightingData.LOCAL_FUNCTION)
                    }
                    checkUpValue(o)
                } else {
                    if (isModuleFile) {
                        newInfoAnnotation(o, "Module field \"${o.name}\"") {
                            it.textAttributes(LuaHighlightingData.FIELD)
                        }
                    } else {
                        newInfoAnnotation(o, "Global variable \"${o.name}\"") {
                            it.textAttributes(LuaHighlightingData.GLOBAL_VAR)
                        }
                    }
                }
            }
        }

        private fun checkUpValue(o: LuaNameExpr) {
            val upValue = isUpValue(o, SearchContext.get(o.project))
            if (upValue) {
                newAnnotation(UPVALUE, o, "Up-value \"${o.name}\"") {
                    it.textAttributes(LuaHighlightingData.UP_VALUE)
                }
            }
        }

        override fun visitIndexExpr(o: LuaIndexExpr) {
            super.visitIndexExpr(o)
            val prefix = o.prefixExpr
            if (prefix is LuaNameExpr && prefix.getUserData(STD_MARKER) != null) {
                newInfoAnnotation(o, "Std apis") {
                    it.textAttributes(LuaHighlightingData.STD_API)
                }
                o.putUserData(STD_MARKER, true)
            } else {
                val id = o.id
                if (id != null) {
                    if (o.parent is LuaCallExpr) {
                        if (o.colon != null) {
                            newInfoAnnotation(id, null) {
                                it.textAttributes(LuaHighlightingData.INSTANCE_METHOD)
                            }
                        } else {
                            newInfoAnnotation(id, null) {
                                it.textAttributes(LuaHighlightingData.STATIC_METHOD)
                            }
                        }
                    } else {
                        if (o.colon != null) {
                            newAnnotation(HighlightSeverity.ERROR, o, "Arguments expected") {
                            }
                        } else {
                            newInfoAnnotation(id, null) {
                                it.textAttributes(LuaHighlightingData.FIELD)
                            }
                        }
                    }
                }
            }
        }
    }

    internal inner class LuaDocElementVisitor : LuaDocVisitor() {
        override fun visitTagClass(o: LuaDocTagClass) {
            super.visitTagClass(o)
            newInfoAnnotation(o.id, null) {
                it.textAttributes(LuaHighlightingData.CLASS_NAME)
            }
        }

        override fun visitTagAlias(o: LuaDocTagAlias) {
            super.visitTagAlias(o)
            val id = o.id ?: return
            newInfoAnnotation(id, null) {
                it.textAttributes(LuaHighlightingData.TYPE_ALIAS)
            }
        }

        override fun visitClassNameRef(o: LuaDocClassNameRef) {
            newInfoAnnotation(o, null) {
                it.textAttributes(LuaHighlightingData.CLASS_REFERENCE)
            }
        }

        override fun visitTagField(o: LuaDocTagField) {
            super.visitTagField(o)
            val id = o.nameIdentifier
            if (id != null) {
                newInfoAnnotation(id, null) {
                    it.textAttributes(LuaHighlightingData.DOC_COMMENT_TAG_VALUE)
                }
            }
        }

        override fun visitParamNameRef(o: LuaDocParamNameRef) {
            newInfoAnnotation(o, null) {
                it.textAttributes(LuaHighlightingData.DOC_COMMENT_TAG_VALUE)
            }
        }
    }
}
