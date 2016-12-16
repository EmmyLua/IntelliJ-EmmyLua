package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.stubs.index.LuaClassIndex;

import java.util.Collection;

/**
 * LuaLookupElement
 * Created by TangZX on 2016/12/16.
 */
public class LuaLookupElement {
    public static void fillTypes(Project project, Collection<LookupElement> results) {
        Collection<String> collection = LuaClassIndex.getInstance().getAllKeys(project);
        collection.forEach(className -> {
            results.add(LookupElementBuilder.create(className).withIcon(LuaIcons.CLASS));
        });
    }
}
