package com.tang.intellij.lua.codeInsight;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
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
        if (element instanceof LuaClassMethodDef) {
            LuaClassMethodDef methodDef = (LuaClassMethodDef) element;
            LuaType type = methodDef.getClassType(new SearchContext(element.getProject()));
            if (type != null) {
                String methodName = methodDef.getName();
                assert methodName != null;
                LuaType superType = type.getSuperClass();
                Project project = methodDef.getProject();
                GlobalSearchScope scope = new ProjectAndLibrariesScope(project);
                while (superType != null) {
                    String superTypeName = superType.getClassNameText();

                    Collection<LuaClassMethodDef> methods = LuaClassMethodIndex.getInstance().get(superTypeName, project, scope);
                    for (LuaClassMethodDef superMethodDef : methods) {
                        if (methodName.equals(superMethodDef.getName())) {

                            NavigationGutterIconBuilder<PsiElement> builder =
                                    NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridingMethod)
                                            .setTargets(superMethodDef)
                                            .setTooltipText("Override in " + superTypeName);
                            result.add(builder.createLineMarkerInfo(element));
                            break;
                        }
                    }
                    superType = superType.getSuperClass();
                }
            }
        }
    }
}
