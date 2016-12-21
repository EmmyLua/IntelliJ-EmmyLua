package com.tang.intellij.lua.lang.type;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import org.jetbrains.annotations.NotNull;

/**
 * 类型说明
 * Created by TangZX on 2016/12/4.
 */
public class LuaType {

    protected LuaType() {

    }

    public LuaType getSuperClass() {
        return null;
    }

    public String getClassNameText() {
        return null;
    }

    public void addMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {
        addMethodCompletions(completionParameters, completionResultSet, true, true);
    }

    protected void addMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean bold, boolean withSuper) {

    }

    protected void addStaticMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean bold, boolean withSuper) {

    }

    public void addFieldCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {
        addFieldCompletions(completionParameters, completionResultSet, true, true);
        addStaticMethodCompletions(completionParameters, completionResultSet, true, true);
    }

    protected void addFieldCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean bold, boolean withSuper) {

    }

    public LuaTypeSet guessFieldType(String propName, Project project, GlobalSearchScope scope) {
        return null;
    }

    public LuaDocFieldDef findField(String fieldName, Project project, GlobalSearchScope scope) {
        return null;
    }

    public LuaClassMethodDef findMethod(String methodName, Project project, GlobalSearchScope scope) {
        return null;
    }

    public LuaClassMethodDef findStaticMethod(String methodName, boolean withSuper, Project project, GlobalSearchScope scope) {
        return null;
    }
}
