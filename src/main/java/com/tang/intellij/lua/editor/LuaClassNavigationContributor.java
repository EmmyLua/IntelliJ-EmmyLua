package com.tang.intellij.lua.editor;

import com.intellij.navigation.GotoClassContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.psi.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Goto Class
 * Created by TangZX on 2016/12/12.
 */
public class LuaClassNavigationContributor implements GotoClassContributor {
    @Nullable
    @Override
    public String getQualifiedName(NavigationItem navigationItem) {
        return "test";
    }

    @Nullable
    @Override
    public String getQualifiedNameSeparator() {
        return ".";
    }

    @NotNull
    @Override
    public String[] getNames(Project project, boolean b) {
        Collection<String> allClasses = LuaClassIndex.getInstance().getAllKeys(project);
        return allClasses.toArray(new String[allClasses.size()]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String s, String s1, Project project, boolean b) {
        LuaDocClassDef classDef = LuaClassIndex.find(s, project, new ProjectAndLibrariesScope(project));
        if (classDef == null)
            return new NavigationItem[0];
        else
            return new NavigationItem[] { classDef };
    }
}
