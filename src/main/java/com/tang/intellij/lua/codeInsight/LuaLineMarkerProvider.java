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

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaClassMethodName;
import com.tang.intellij.lua.psi.search.LuaClassInheritorsSearch;
import com.tang.intellij.lua.psi.search.LuaOverridingMethodsSearch;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * line marker
 * Created by tangzx on 2016/12/11.
 */
public class LuaLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        if (element instanceof LuaClassMethodName) {
            LuaClassMethodName classMethodName = (LuaClassMethodName) element;
            LuaClassMethodDef methodDef = PsiTreeUtil.getParentOfType(element, LuaClassMethodDef.class);
            assert methodDef != null;
            Project project = methodDef.getProject();
            SearchContext context = new SearchContext(project);
            LuaType type = methodDef.getClassType(context);

            //OverridingMethod
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
                        result.add(builder.createLineMarkerInfo(classMethodName.getId()));
                        break;
                    }
                    superType = superType.getSuperClass(context);
                }
            }

            // OverridenMethod
            Query<LuaClassMethodDef> search = LuaOverridingMethodsSearch.search(methodDef);
            Collection<LuaClassMethodDef> all = search.findAll();
            if (!all.isEmpty()) {
                NavigationGutterIconBuilder<PsiElement> builder =
                        NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridenMethod).setTargets(all);
                result.add(builder.createLineMarkerInfo(classMethodName.getId()));
            }
        }
        else if (element instanceof LuaDocClassDef) {
            LuaDocClassDef docClassDef = (LuaDocClassDef) element;
            LuaType classType = docClassDef.getClassType();
            Project project = element.getProject();
            Query<LuaDocClassDef> query = LuaClassInheritorsSearch.search(GlobalSearchScope.allScope(project), project, classType.getClassName());
            Collection<LuaDocClassDef> all = query.findAll();
            if (!all.isEmpty()) {
                NavigationGutterIconBuilder<PsiElement> builder =
                        NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridenMethod).setTargets(all);
                result.add(builder.createLineMarkerInfo(docClassDef));
            }
        }
    }
}
