package com.tang.intellij.lua.editor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.refactoring.rename.NameSuggestionProvider;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaNameDef;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LuaNameDef implements PsiNameIdentifierOwner
 *
 * Created by TangZX on 2016/12/20.
 */
public class LuaNameSuggestionProvider implements NameSuggestionProvider {
    @Nullable
    @Override
    public SuggestedNameInfo getSuggestedNames(PsiElement psiElement, @Nullable PsiElement psiElement1, Set<String> set) {
        if (psiElement instanceof LuaNameDef) {
            LuaNameDef nameDef = (LuaNameDef) psiElement;
            LuaTypeSet typeSet = nameDef.resolveType(new SearchContext(psiElement.getProject()));
            if (typeSet != null) {
                Set<String> classNames = new HashSet<>();

                for (LuaType type : typeSet.getTypes()) {
                    LuaType cur = type;
                    while (cur != null) {
                        String className = cur.getClassNameText();
                        if (className != null)
                            classNames.add(className);
                        cur = cur.getSuperClass();
                    }
                }

                for (String className : classNames) {
                    List<String> strings = NameUtil.getSuggestionsByName(className, "", "", false, false, false);
                    set.addAll(strings);
                }
            }
        }
        return null;
    }
}
