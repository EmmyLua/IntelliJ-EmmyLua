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

package com.tang.intellij.lua.codeInsight

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.SeparatorPlacement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Function
import com.intellij.util.FunctionUtil
import com.intellij.util.Query
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.search.LuaClassInheritorsSearch
import com.tang.intellij.lua.psi.search.LuaOverridingMethodsSearch
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.ty.TyClass

/**
 * line marker
 * Created by tangzx on 2016/12/11.
 */
class LuaLineMarkerProvider(private val daemonSettings: DaemonCodeAnalyzerSettings, private val colorsManager:EditorColorsManager) : LineMarkerProvider {

    private fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in LineMarkerInfo<*>>) {
        if (element is LuaClassMethodName) {
            val methodDef = PsiTreeUtil.getParentOfType(element, LuaClassMethod::class.java)!!
            val project = methodDef.project
            val context = SearchContext(project)
            val type = methodDef.guessClassType(context)

            //OverridingMethod
            val classMethodNameId = element.id
            if (type != null) {
                val methodName = methodDef.name!!
                var superType = type.getSuperClass(context)

                while (superType != null && superType is TyClass) {
                    val superTypeName = superType.className
                    val superMethod = LuaClassMemberIndex.findMethod(superTypeName, methodName, context)
                    if (superMethod != null) {
                        val builder = NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridingMethod)
                                .setTargets(superMethod)
                                .setTooltipText("Override in " + superTypeName)
                        result.add(builder.createLineMarkerInfo(classMethodNameId))
                        break
                    }
                    superType = superType.getSuperClass(context)
                }
            }

            // OverridenMethod
            val search = LuaOverridingMethodsSearch.search(methodDef)
            if (search.findFirst() != null) {
                result.add(LineMarkerInfo(methodDef,
                        element.textRange,
                        AllIcons.Gutter.OverridenMethod,
                        Pass.LINE_MARKERS,
                        overridingMethodTooltipProvider,
                        overridingMethodNavigator,
                        GutterIconRenderer.Alignment.CENTER))
            }

            //line separator
            if (daemonSettings.SHOW_METHOD_SEPARATORS) {
                //todo : module file method
                val startOffset = methodDef.node.startOffset
                val lineSeparator = LineMarkerInfo(element, TextRange(startOffset, startOffset), null, Pass.LINE_MARKERS, null, null, GutterIconRenderer.Alignment.RIGHT)
                lineSeparator.separatorColor = colorsManager.globalScheme.getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR)
                lineSeparator.separatorPlacement = SeparatorPlacement.TOP
                result.add(lineSeparator)
            }
        } else if (element is LuaDocClassDef) {
            val classType = element.type
            val project = element.getProject()
            val query = LuaClassInheritorsSearch.search(GlobalSearchScope.allScope(project), project, classType.className)
            if (query.findFirst() != null) {
                result.add(LineMarkerInfo(element,
                        element.textRange,
                        AllIcons.Gutter.OverridenMethod,
                        Pass.LINE_MARKERS,
                        subclassTooltipProvider,
                        subclassNavigator,
                        GutterIconRenderer.Alignment.CENTER))
            }

            // class 标记
            val startOffset = element.getTextOffset()
            val classIcon = LineMarkerInfo<PsiElement>(element,
                    TextRange(startOffset, startOffset),
                    LuaIcons.CLASS,
                    Pass.LINE_MARKERS, null, null,
                    GutterIconRenderer.Alignment.CENTER)
            result.add(classIcon)
        } else if (element is LuaCallExpr) {
            val expr = element.expr
            val reference = expr.reference
            if (reference != null) {
                val resolve = reference.resolve()
                if (resolve != null) {
                    var cur: PsiElement? = element
                    while (cur != null) {
                        val bodyOwner = PsiTreeUtil.getParentOfType(cur, LuaFuncBodyOwner::class.java)
                        if (bodyOwner === resolve) {
                            result.add(LineMarkerInfo<PsiElement>(element,
                                    element.getTextRange(),
                                    AllIcons.Gutter.RecursiveMethod,
                                    Pass.LINE_MARKERS,
                                    FunctionUtil.constant("Recursive call"), null,
                                    GutterIconRenderer.Alignment.CENTER))
                            break
                        }
                        cur = bodyOwner
                    }
                }
            }
        } else if (element is LuaReturnStat) {
            val exprList = element.exprList
            if (exprList != null) {
                for (psiElement in exprList.children) {
                    if (psiElement is LuaCallExpr) {
                        val returnKeyWord = element.firstChild
                        result.add(LineMarkerInfo(returnKeyWord,
                                returnKeyWord.textRange,
                                AllIcons.General.HideRightHover,
                                Pass.LINE_MARKERS,
                                FunctionUtil.constant("Tail call"), null,
                                GutterIconRenderer.Alignment.CENTER))
                        break
                    }
                }
            }
        }
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        return null
    }

    override fun collectSlowLineMarkers(list: List<PsiElement>, collection: MutableCollection<LineMarkerInfo<*>>) {
        for (element in list) {
            collectNavigationMarkers(element, collection)
        }
    }

    companion object {

        private val overridingMethodTooltipProvider = Function<LuaClassMethod, String> {
            /*final StringBuilder builder = new StringBuilder("<html>Is overridden in:");
            LuaClassMethod methodDef = PsiTreeUtil.getParentOfType(methodName, LuaClassMethod.class);
            assert methodDef != null;
            LuaOverridingMethodsSearch.Companion.search(methodDef).forEach(luaClassMethodDef -> {
            });
            return builder.toString();*/
            "<html>Is overridden in"
        }

        private val overridingMethodNavigator = object : LuaLineMarkerNavigator<LuaClassMethod, LuaClassMethod>() {

            override fun getTitle(elt: LuaClassMethod): String {
                return "Choose Overriding Method of " + elt.name!!
            }

            override fun search(elt: LuaClassMethod): Query<LuaClassMethod>? {
                return LuaOverridingMethodsSearch.search(elt)
            }
        }

        private val subclassTooltipProvider = Function<LuaDocClassDef, String> { it.name }

        private val subclassNavigator = object : LuaLineMarkerNavigator<LuaDocClassDef, LuaDocClassDef>() {
            override fun getTitle(elt: LuaDocClassDef): String {
                return "Choose Subclass of " + elt.name
            }

            override fun search(elt: LuaDocClassDef): Query<LuaDocClassDef> {
                val project = elt.project
                return LuaClassInheritorsSearch.search(GlobalSearchScope.allScope(project), project, elt.name)
            }
        }
    }
}
