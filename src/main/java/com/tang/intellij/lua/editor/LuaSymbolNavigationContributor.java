package com.tang.intellij.lua.editor;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.util.containers.HashSet;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Goto Symbol
 * Created by TangZX on 2016/12/12.
 */
public class LuaSymbolNavigationContributor implements ChooseByNameContributor {
    @NotNull
    @Override
    public String[] getNames(Project project, boolean b) {
        HashSet<String> nameSet = new HashSet<>();
        LuaShortNameIndex.getInstance().processAllKeys(project, s -> {
            nameSet.add(s);
            return true;
        });
        return nameSet.toArray(new String[nameSet.size()]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String s, String s1, Project project, boolean b) {
        Collection<NavigatablePsiElement> elements = LuaShortNameIndex.find(s, new SearchContext(project));
        return elements.toArray(new NavigatablePsiElement[elements.size()]);
    }
}
