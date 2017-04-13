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

package com.tang.intellij.lua.codeInsight;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.FunctionUtil;
import com.intellij.util.Query;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.psi.search.LuaClassInheritorsSearch;
import com.tang.intellij.lua.psi.search.LuaOverridingMethodsSearch;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaClassMethodStub;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * line marker
 * Created by tangzx on 2016/12/11.
 */
public class LuaLineMarkerProvider implements LineMarkerProvider {

    private static final Function<LuaClassMethodName, String> overridingMethodTooltipProvider = (methodName) -> {
        final StringBuilder builder = new StringBuilder("<html>Is overridden in:");
        LuaClassMethodDef methodDef = PsiTreeUtil.getParentOfType(methodName, LuaClassMethodDef.class);
        assert methodDef != null;
        LuaOverridingMethodsSearch.search(methodDef).forEach(luaClassMethodDef -> {
            LuaClassMethodStub stub = luaClassMethodDef.getStub();
            if (stub != null) {
                builder.append("<br>");
                builder.append(stub.getClassName());
            }
        });

        return builder.toString();
    };

    private static final LuaLineMarkerNavigator<LuaClassMethodName, LuaClassMethodDef> overridingMethodNavigator = new LuaLineMarkerNavigator<LuaClassMethodName, LuaClassMethodDef>() {

        @Override
        protected String getTitle(LuaClassMethodName elt) {
            return "Choose Overriding Method of " + elt.getName();
        }

        @Nullable
        @Override
        protected Query<LuaClassMethodDef> search(LuaClassMethodName elt) {
            LuaClassMethodDef def = PsiTreeUtil.getParentOfType(elt, LuaClassMethodDef.class);
            if (def == null)
                return null;
            return LuaOverridingMethodsSearch.search(def);
        }
    };

    private static final Function<LuaDocClassDef, String> subclassTooltipProvider = LuaDocClassDef::getName;

    private static final LuaLineMarkerNavigator<LuaDocClassDef, LuaDocClassDef> subclassNavigator = new LuaLineMarkerNavigator<LuaDocClassDef, LuaDocClassDef>() {
        @Override
        protected String getTitle(LuaDocClassDef elt) {
            return "Choose Subclass of " + elt.getName();
        }

        @NotNull
        @Override
        protected Query<LuaDocClassDef> search(LuaDocClassDef elt) {
            Project project = elt.getProject();
            return LuaClassInheritorsSearch.search(GlobalSearchScope.allScope(project), project, elt.getName());
        }
    };

    private void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super LineMarkerInfo> result) {
        if (element instanceof LuaClassMethodName) {
            LuaClassMethodName classMethodName = (LuaClassMethodName) element;
            LuaClassMethodDef methodDef = PsiTreeUtil.getParentOfType(element, LuaClassMethodDef.class);
            assert methodDef != null;
            Project project = methodDef.getProject();
            SearchContext context = new SearchContext(project);
            LuaType type = methodDef.getClassType(context);

            //OverridingMethod
            PsiElement classMethodNameId = classMethodName.getId();
            if (type != null) {
                String methodName = methodDef.getName();
                assert methodName != null;
                LuaType superType = type.getSuperClass(context);

                while (superType != null) {
                    String superTypeName = superType.getClassName();
                    LuaClassMethodDef superMethod = LuaClassMethodIndex.findMethodWithName(superTypeName, methodName, context);
                    if (superMethod != null) {
                        NavigationGutterIconBuilder<PsiElement> builder =
                                NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridingMethod)
                                        .setTargets(superMethod)
                                        .setTooltipText("Override in " + superTypeName);
                        result.add(builder.createLineMarkerInfo(classMethodNameId));
                        break;
                    }
                    superType = superType.getSuperClass(context);
                }
            }

            // OverridenMethod
            Query<LuaClassMethodDef> search = LuaOverridingMethodsSearch.search(methodDef);
            if (search.findFirst() != null) {
                result.add(new LineMarkerInfo<>(classMethodName,
                        classMethodName.getTextRange(),
                        AllIcons.Gutter.OverridenMethod,
                        Pass.LINE_MARKERS,
                        overridingMethodTooltipProvider,
                        overridingMethodNavigator,
                        GutterIconRenderer.Alignment.CENTER));
            }

            //line separator
            /*LineMarkerInfo lineSeparator = new LineMarkerInfo<>(classMethodName, classMethodName.getNode().getStartOffset(), null, Pass.LINE_MARKERS, null, null);
            lineSeparator.separatorColor = EditorColorsManager.getInstance().getGlobalScheme().getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
            lineSeparator.separatorPlacement = SeparatorPlacement.TOP;
            result.add(lineSeparator);*/
        }
        else if (element instanceof LuaDocClassDef) {
            LuaDocClassDef docClassDef = (LuaDocClassDef) element;
            LuaType classType = docClassDef.getClassType();
            Project project = element.getProject();
            Query<LuaDocClassDef> query = LuaClassInheritorsSearch.search(GlobalSearchScope.allScope(project), project, classType.getClassName());
            if (query.findFirst() != null) {
                result.add(new LineMarkerInfo<>(docClassDef,
                        docClassDef.getTextRange(),
                        AllIcons.Gutter.OverridenMethod,
                        Pass.LINE_MARKERS,
                        subclassTooltipProvider,
                        subclassNavigator,
                        GutterIconRenderer.Alignment.CENTER));
            }

            // class 标记
            int startOffset = element.getTextOffset();
            LineMarkerInfo classIcon = new LineMarkerInfo<>(element,
                    new TextRange(startOffset, startOffset),
                    LuaIcons.CLASS,
                    Pass.LINE_MARKERS,
                    null,
                    null,
                    GutterIconRenderer.Alignment.CENTER);
            result.add(classIcon);
        }
        else if (element instanceof LuaCallExpr) {
            LuaCallExpr callExpr = (LuaCallExpr) element;
            LuaExpr expr = callExpr.getExpr();
            PsiReference reference = expr.getReference();
            if (reference != null) {
                PsiElement resolve = reference.resolve();
                if (resolve != null) {
                    PsiElement cur = callExpr;
                    while (cur != null) {
                        LuaFuncBodyOwner bodyOwner = PsiTreeUtil.getParentOfType(cur, LuaFuncBodyOwner.class);
                        if (bodyOwner == resolve) {
                            result.add(new LineMarkerInfo<>(element,
                                    element.getTextRange(),
                                    AllIcons.Gutter.RecursiveMethod,
                                    Pass.LINE_MARKERS,
                                    FunctionUtil.constant("Recursive call"),
                                    null,
                                    GutterIconRenderer.Alignment.CENTER));
                            break;
                        }
                        cur = bodyOwner;
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {
        for (PsiElement element : list) {
            collectNavigationMarkers(element, collection);
        }
    }
}
