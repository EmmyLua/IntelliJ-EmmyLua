package com.tang.intellij.lua.editor;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.util.containers.HashSet;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.stubs.index.LuaGlobalFuncIndex;
import org.jetbrains.annotations.NotNull;

/**
 * Goto Symbol
 * Created by TangZX on 2016/12/12.
 */
public class LuaSymbolNavigationContributor implements ChooseByNameContributor {
    @NotNull
    @Override
    public String[] getNames(Project project, boolean b) {
        HashSet<String> nameSet = new HashSet<>();
        nameSet.addAll(LuaGlobalFuncIndex.getInstance().getAllKeys(project));
        return nameSet.toArray(new String[nameSet.size()]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String s, String s1, Project project, boolean b) {
        LuaGlobalFuncDef def = LuaGlobalFuncIndex.find(s, project, new ProjectAndLibrariesScope(project));
        if (def == null)
            return NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY;
        else
            return new NavigationItem[]{ def };
    }
}
