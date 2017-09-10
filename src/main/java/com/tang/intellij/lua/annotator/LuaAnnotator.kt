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

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
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
    private val STD_MARKER = Key.create<Boolean>("lua.std.marker")
    private var isModuleFile: Boolean = false

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        myHolder = annotationHolder
        if (psiElement is LuaDocPsiElement) {
            psiElement.accept(docVisitor)
        } else if (psiElement is LuaPsiElement) {
            val psiFile = psiElement.containingFile
            isModuleFile = if (psiFile is LuaFile) { psiFile.moduleName != null } else false
            psiElement.accept(luaVisitor)
        }
        myHolder = null
    }

    internal inner class LuaElementVisitor : LuaVisitor() {

        override fun visitUncompletedStat(o: LuaUncompletedStat) {
            myHolder!!.createErrorAnnotation(o, "Uncompleted")
        }

        override fun visitLocalFuncDef(o: LuaLocalFuncDef) {
            val name = o.nameIdentifier

            if (name != null) {
                val annotation = myHolder!!.createInfoAnnotation(name, null)
                annotation.textAttributes = LuaHighlightingData.LOCAL_VAR
            }
        }

        override fun visitLocalDef(o: LuaLocalDef) {
            val nameList = o.nameList
            if (nameList != null) {
                var child: PsiElement? = nameList.firstChild
                while (child != null) {
                    if (child is LuaNameDef) {
                        val annotation = myHolder!!.createInfoAnnotation(child, null)
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
                val annotation = myHolder!!.createInfoAnnotation(id, null)
                annotation.textAttributes = LuaHighlightingData.FIELD
            }
        }

        override fun visitFuncDef(o: LuaFuncDef) {
            val name = o.nameIdentifier
            if (name != null) {
                val annotation = myHolder!!.createInfoAnnotation(name, null)
                annotation.textAttributes = if (isModuleFile) LuaHighlightingData.INSTANCE_METHOD else LuaHighlightingData.GLOBAL_FUNCTION
            }
        }

        override fun visitClassMethodName(o: LuaClassMethodName) {
            val annotation = myHolder!!.createInfoAnnotation(o.id, null)
            if (o.dot != null) {
                annotation.textAttributes = LuaHighlightingData.STATIC_METHOD
            } else {
                annotation.textAttributes = LuaHighlightingData.INSTANCE_METHOD
            }
        }

        override fun visitParamNameDef(o: LuaParamNameDef) {
            if (o.textMatches(Constants.WORD_UNDERLINE))
                return

            val search = ReferencesSearch.search(o, o.useScope)
            if (search.findFirst() == null) {
                myHolder!!.createInfoAnnotation(o, "Unused parameter : " + o.text)
                //annotation.setTextAttributes(CodeInsightColors.WEAK_WARNING_ATTRIBUTES);
            } else {
                val annotation = myHolder!!.createInfoAnnotation(o, null)
                annotation.setTextAttributes(LuaHighlightingData.PARAMETER)
            }
        }

        override fun visitNameExpr(o: LuaNameExpr) {
            val id = o.firstChild

            val res = resolve(o, SearchContext(o.project))
            if (res != null) { //std api highlighting
                val containingFile = res.containingFile
                if (LuaFileUtil.isStdLibFile(containingFile.virtualFile, o.project)) {
                    val annotation = myHolder!!.createInfoAnnotation(o, null)
                    annotation.textAttributes = LuaHighlightingData.STD_API
                    o.putUserData(STD_MARKER, true)
                    return
                }
            }

            if (res is LuaParamNameDef) {
                val annotation = myHolder!!.createInfoAnnotation(o, null)
                annotation.textAttributes = LuaHighlightingData.PARAMETER
                checkUpValue(o)
            } else if (res is LuaFuncDef) {
                val annotation = myHolder!!.createInfoAnnotation(o, null)
                val resolvedFile = res.containingFile
                if (resolvedFile !is LuaFile || resolvedFile.moduleName == null)
                    annotation.textAttributes = LuaHighlightingData.GLOBAL_FUNCTION
            } else {
                if (id.textMatches(Constants.WORD_SELF)) {
                    val annotation = myHolder!!.createInfoAnnotation(o, null)
                    annotation.textAttributes = LuaHighlightingData.SELF
                    checkUpValue(o)
                } else if (res is LuaNameDef || res is LuaLocalFuncDef) { //Local
                    val annotation = myHolder!!.createInfoAnnotation(o, null)
                    annotation.textAttributes = LuaHighlightingData.LOCAL_VAR
                    checkUpValue(o)
                } else { // 未知的，视为Global
                    val annotation = myHolder!!.createInfoAnnotation(o, null)
                    annotation.textAttributes = if (isModuleFile) LuaHighlightingData.FIELD else LuaHighlightingData.GLOBAL_VAR
                }
            }
        }

        private fun checkUpValue(o: LuaNameExpr) {
            val upValue = isUpValue(o, SearchContext(o.project))
            if (upValue) {
                val annotation = myHolder!!.createInfoAnnotation(o, null)
                annotation.textAttributes = LuaHighlightingData.UP_VALUE
            }
        }

        override fun visitIndexExpr(o: LuaIndexExpr) {
            super.visitIndexExpr(o)
            val prefix = o.prefixExpr
            if (prefix is LuaNameExpr && prefix.getUserData(STD_MARKER) != null) {
                val annotation = myHolder!!.createInfoAnnotation(o, null)
                annotation.textAttributes = LuaHighlightingData.STD_API
                o.putUserData(STD_MARKER, true)
            } else {
                val id = o.id
                if (id != null) {
                    val annotation = myHolder!!.createInfoAnnotation(id, null)
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
        override fun visitClassDef(o: LuaDocClassDef) {
            super.visitClassDef(o)
            val annotation = myHolder!!.createInfoAnnotation(o.id, null)
            annotation.textAttributes = DefaultLanguageHighlighterColors.CLASS_NAME
        }

        override fun visitClassNameRef(o: LuaDocClassNameRef) {
            val annotation = myHolder!!.createInfoAnnotation(o, null)
            annotation.textAttributes = DefaultLanguageHighlighterColors.CLASS_REFERENCE
        }

        override fun visitFieldDef(o: LuaDocFieldDef) {
            super.visitFieldDef(o)
            val id = o.nameIdentifier
            if (id != null) {
                val annotation = myHolder!!.createInfoAnnotation(id, null)
                annotation.textAttributes = LuaHighlightingData.DOC_COMMENT_TAG_VALUE
            }
        }

        override fun visitParamNameRef(o: LuaDocParamNameRef) {
            val annotation = myHolder!!.createInfoAnnotation(o, null)
            annotation.textAttributes = LuaHighlightingData.DOC_COMMENT_TAG_VALUE
        }
    }
}
