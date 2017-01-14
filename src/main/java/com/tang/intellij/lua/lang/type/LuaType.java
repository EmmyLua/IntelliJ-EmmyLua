package com.tang.intellij.lua.lang.type;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.editor.completion.FuncInsertHandler;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 类型说明
 * Created by TangZX on 2016/12/4.
 */
public class LuaType {

    private PsiElement element;

    protected LuaType(PsiElement element) {

        this.element = element;
    }

    public LuaType getSuperClass() {
        return null;
    }

    public String getClassNameText() {
        return null;
    }

    protected Project getProject() {
        if (element == null) return null;
        return element.getProject();
    }

    public void addMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean useAsField) {
        addMethodCompletions(completionParameters, completionResultSet, true, true, useAsField);
    }

    protected void addMethodCompletions(@NotNull CompletionParameters completionParameters,
                                        @NotNull CompletionResultSet completionResultSet,
                                        boolean bold,
                                        boolean withSuper,
                                        boolean useAsField) {
        Project project = getProject();
        if (project == null)
            return;
        String clazzName = getClassNameText();
        if (clazzName == null)
            return;

        Collection<LuaClassMethodDef> list = LuaClassMethodIndex.getInstance().get(clazzName, project, new ProjectAndLibrariesScope(project));
        for (LuaClassMethodDef def : list) {
            String methodName = def.getName();
            if (methodName != null) {
                LookupElementBuilder elementBuilder = LookupElementBuilder.create(methodName)
                        .withIcon(LuaIcons.CLASS_METHOD)
                        .withTypeText(clazzName);
                if (!useAsField)
                    elementBuilder = elementBuilder.withInsertHandler(new FuncInsertHandler(def.getFuncBody()));
                if (bold)
                    elementBuilder = elementBuilder.bold();

                completionResultSet.addElement(elementBuilder);
            }
        }

        if (withSuper) {
            LuaType superType = getSuperClass();
            if (superType != null)
                superType.addMethodCompletions(completionParameters, completionResultSet, false, true, useAsField);
        }
    }



    protected void addStaticMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean bold, boolean withSuper) {
        Project project = getProject();
        if (project == null)
            return;
        String clazzName = getClassNameText();
        if (clazzName == null)
            return;
        Collection<LuaClassMethodDef> list = LuaClassMethodIndex.findStaticMethods(clazzName, project, new ProjectAndLibrariesScope(project));
        for (LuaClassMethodDef def : list) {
            String methodName = def.getName();
            if (methodName != null) {
                LookupElementBuilder elementBuilder = LookupElementBuilder.create(methodName)
                        .withIcon(LuaIcons.CLASS_METHOD)
                        .withInsertHandler(new FuncInsertHandler(def.getFuncBody()))
                        .withTypeText(clazzName)
                        .withItemTextUnderlined(true);
                if (bold)
                    elementBuilder = elementBuilder.bold();

                completionResultSet.addElement(elementBuilder);
            }
        }

        if (withSuper) {
            LuaType superType = getSuperClass();
            if (superType != null)
                superType.addStaticMethodCompletions(completionParameters, completionResultSet, false, true);
        }
    }

    public void addFieldCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {
        addFieldCompletions(completionParameters, completionResultSet, true, true);
        addStaticMethodCompletions(completionParameters, completionResultSet, true, true);
        addMethodCompletions(completionParameters, completionResultSet, true);
    }

    protected void addFieldCompletions(@NotNull CompletionParameters completionParameters,
                                       @NotNull CompletionResultSet completionResultSet,
                                       boolean bold,
                                       boolean withSuper) {
        String clazzName = getClassNameText();
        if (clazzName == null)
            return;
        Project project = getProject();
        if (project == null)
            return;

        Collection<LuaClassField> list = LuaClassFieldIndex.getInstance().get(clazzName, project, new ProjectAndLibrariesScope(project));

        for (LuaClassField fieldName : list) {
            String name = fieldName.getFieldName();
            if (name == null)
                continue;

            LookupElementBuilder elementBuilder = LookupElementBuilder.create(name)
                    .withIcon(LuaIcons.CLASS_FIELD)
                    .withTypeText(clazzName);
            if (bold)
                elementBuilder = elementBuilder.bold();

            completionResultSet.addElement(elementBuilder);
        }

        // super
        if (withSuper) {
            LuaType superType = getSuperClass();
            if (superType != null)
                superType.addFieldCompletions(completionParameters, completionResultSet, false, true);
        }
    }

    public LuaTypeSet guessFieldType(String propName, Project project, GlobalSearchScope scope) {
        LuaTypeSet set = null;
        LuaClassField fieldDef = LuaClassFieldIndex.find(getClassNameText(), propName, project, scope);
        if (fieldDef != null)
            set = fieldDef.resolveType();
        else {
            LuaType superType = getSuperClass();
            if (superType != null)
                set = superType.guessFieldType(propName, project, scope);
        }

        return set;
    }

    public LuaClassField findField(String fieldName, Project project, GlobalSearchScope scope) {
        String className = getClassNameText();
        LuaClassField def = LuaClassFieldIndex.find(className, fieldName, project, scope);
        if (def == null) {
            LuaType superType = getSuperClass();
            if (superType != null)
                def = superType.findField(fieldName, project, scope);
        }
        return def;
    }

    public LuaClassMethodDef findMethod(String methodName, Project project, GlobalSearchScope scope) {
        String className = getClassNameText();
        LuaClassMethodDef def = LuaClassMethodIndex.findMethodWithName(className, methodName, project, scope);
        if (def == null) { // static
            def = LuaClassMethodIndex.findStaticMethod(className, methodName, project, scope);
        }
        if (def == null) { // super
            LuaType superType = getSuperClass();
            if (superType != null)
                def = superType.findMethod(methodName, project, scope);
        }
        return def;
    }

    public LuaClassMethodDef findStaticMethod(String methodName, boolean withSuper, Project project, GlobalSearchScope scope) {
        String className = getClassNameText();
        LuaClassMethodDef def = LuaClassMethodIndex.findStaticMethod(className, methodName, project, scope);
        if (def == null && withSuper) {
            LuaType superType = getSuperClass();
            if (superType != null)
                def = superType.findStaticMethod(methodName, true, project, scope);
        }
        return def;
    }
}
