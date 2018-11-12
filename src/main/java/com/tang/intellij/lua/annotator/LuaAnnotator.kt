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

import com.intellij.lang.annotation.Annotation
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

    private fun createInfoAnnotation(psi: PsiElement, msg: String? = null): Annotation {
        return myHolder!!.createInfoAnnotation(psi, msg)
    }

    internal inner class LuaElementVisitor : LuaVisitor() {

        override fun visitExprStat(o: LuaExprStat) {
            if (o.expr !is LuaCallExpr) {
                if (o.containingFile !is LuaExprCodeFragment)
                    myHolder!!.createErrorAnnotation(o, "syntax error")
            } else super.visitExprStat(o)
        }

        override fun visitLocalFuncDef(o: LuaLocalFuncDef) {
            val name = o.nameIdentifier

            if (name != null) {
                val annotation = createInfoAnnotation(name, "Local function \"${o.name}\"")
                annotation.textAttributes = LuaHighlightingData.LOCAL_VAR
            }
        }

        override fun visitLocalDef(o: LuaLocalDef) {
            val nameList = o.nameList
            if (nameList != null) {
                var child: PsiElement? = nameList.firstChild
                while (child != null) {
                    if (child is LuaNameDef) {
                        val annotation = createInfoAnnotation(child, "Local variable \"${child.name}\"")
                        annotation.textAttributes = LuaHighlightingData.LOCAL_VAR
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
                val annotation = createInfoAnnotation(id)
                annotation.textAttributes = LuaHighlightingData.FIELD
            }
        }

        override fun visitFuncDef(o: LuaFuncDef) {
            val name = o.nameIdentifier
            if (name != null && o.forwardDeclaration == null) {
                if (isModuleFile) {
                    val annotation = createInfoAnnotation(name, "Module function \"${o.name}\"")
                    annotation.textAttributes = LuaHighlightingData.INSTANCE_METHOD
                } else {
                    val annotation = createInfoAnnotation(name, "Global function \"${o.name}\"")
                    annotation.textAttributes = LuaHighlightingData.GLOBAL_FUNCTION
                }
            }
        }

        override fun visitClassMethodName(o: LuaClassMethodName) {
            val id = o.id ?: return
            val annotation = createInfoAnnotation(id)
            if (o.dot != null) {
                annotation.textAttributes = LuaHighlightingData.STATIC_METHOD
            } else {
                annotation.textAttributes = LuaHighlightingData.INSTANCE_METHOD
            }
        }

        override fun visitParamNameDef(o: LuaParamNameDef) {
            val annotation = createInfoAnnotation(o, "Parameter : \"${o.name}\"")
            annotation.textAttributes = LuaHighlightingData.PARAMETER
        }

        override fun visitNameExpr(o: LuaNameExpr) {
            val id = o.firstChild

            val res = resolve(o, SearchContext(o.project))
            if (res != null) { //std api highlighting
                val containingFile = res.containingFile
                if (LuaFileUtil.isStdLibFile(containingFile.virtualFile, o.project)) {
                    val annotation = createInfoAnnotation(o, "Std apis")
                    annotation.textAttributes = LuaHighlightingData.STD_API
                    o.putUserData(STD_MARKER, true)
                    return
                }
            }

            if (res is LuaParamNameDef) {
                val annotation = createInfoAnnotation(o, "Parameter : \"${res.name}\"")
                annotation.textAttributes = LuaHighlightingData.PARAMETER
                checkUpValue(o)
            } else if (res is LuaFuncDef) {
                val resolvedFile = res.containingFile
                if (resolvedFile !is LuaPsiFile || resolvedFile.moduleName == null) {
                    val annotation = createInfoAnnotation(o, "Global function : \"${res.name}\"")
                    annotation.textAttributes = LuaHighlightingData.GLOBAL_FUNCTION
                } else {
                    createInfoAnnotation(o, "Module function : \"${res.name}\"")
                }
            } else {
                if (id.textMatches(Constants.WORD_SELF)) {
                    val annotation = createInfoAnnotation(o)
                    annotation.textAttributes = LuaHighlightingData.SELF
                    checkUpValue(o)
                } else if (res is LuaNameDef) {
                    val annotation = createInfoAnnotation(o, "Local variable \"${o.name}\"")
                    annotation.textAttributes = LuaHighlightingData.LOCAL_VAR
                    checkUpValue(o)
                } else if (res is LuaLocalFuncDef) {
                    val annotation = createInfoAnnotation(o, "Local function \"${o.name}\"")
                    annotation.textAttributes = LuaHighlightingData.LOCAL_VAR
                    checkUpValue(o)
                } else {
                    if (isModuleFile) {
                        val annotation = createInfoAnnotation(o, "Module field \"${o.name}\"")
                        annotation.textAttributes = LuaHighlightingData.FIELD
                    } else {
                        val annotation = createInfoAnnotation(o, "Global variable \"${o.name}\"")
                        annotation.textAttributes = LuaHighlightingData.GLOBAL_VAR
                    }
                }
            }
        }

        private fun checkUpValue(o: LuaNameExpr) {
            val upValue = isUpValue(o, SearchContext(o.project))
            if (upValue) {
                val annotation = myHolder?.createAnnotation(UPVALUE, o.textRange, "Up-value \"${o.name}\"")
                annotation?.textAttributes = LuaHighlightingData.UP_VALUE
            }
        }

        override fun visitIndexExpr(o: LuaIndexExpr) {
            super.visitIndexExpr(o)
            val prefix = o.prefixExpr
            if (prefix is LuaNameExpr && prefix.getUserData(STD_MARKER) != null) {
                val annotation = createInfoAnnotation(o, "Std apis")
                annotation.textAttributes = LuaHighlightingData.STD_API
                o.putUserData(STD_MARKER, true)
            } else {
                val id = o.id
                if (id != null) {
                    val annotation = createInfoAnnotation(id, null)
                    if (o.parent is LuaCallExpr) {
                        if (o.colon != null) {
                            annotation.textAttributes = LuaHighlightingData.INSTANCE_METHOD
                        } else {
                            annotation.textAttributes = LuaHighlightingData.STATIC_METHOD
                        }
                    } else {
                        if (o.colon != null) {
                            myHolder!!.createErrorAnnotation(o, "Arguments expected")
                        } else {
                            annotation.setTextAttributes(LuaHighlightingData.FIELD)
                        }
                    }
                }
            }
        }
    }

    internal inner class LuaDocElementVisitor : LuaDocVisitor() {
        override fun visitTagClass(o: LuaDocTagClass) {
            super.visitTagClass(o)
            val annotation = createInfoAnnotation(o.id, null)
            annotation.textAttributes = LuaHighlightingData.CLASS_NAME
        }

        override fun visitClassNameRef(o: LuaDocClassNameRef) {
            val annotation = createInfoAnnotation(o, null)
            annotation.textAttributes = LuaHighlightingData.CLASS_REFERENCE
        }

        override fun visitTagField(o: LuaDocTagField) {
            super.visitTagField(o)
            val id = o.nameIdentifier
            if (id != null) {
                val annotation = createInfoAnnotation(id, null)
                annotation.textAttributes = LuaHighlightingData.DOC_COMMENT_TAG_VALUE
            }
        }

        override fun visitParamNameRef(o: LuaDocParamNameRef) {
            val annotation = createInfoAnnotation(o, null)
            annotation.textAttributes = LuaHighlightingData.DOC_COMMENT_TAG_VALUE
        }
    }
}
