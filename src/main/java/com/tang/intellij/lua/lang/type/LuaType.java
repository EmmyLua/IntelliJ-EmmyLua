package com.tang.intellij.lua.lang.type;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.editor.completion.FuncInsertHandler;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.search.SearchContext;
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

    public LuaType getSuperClass(SearchContext context) {
        return null;
    }

    public String getClassNameText() {
        return null;
    }

    protected Project getProject() {
        if (element == null) return null;
        return element.getProject();
    }

    public void addMethodCompletions(@NotNull CompletionParameters completionParameters,
                                     @NotNull CompletionResultSet completionResultSet,
                                     boolean useAsField) {
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
            if (methodName != null && completionResultSet.getPrefixMatcher().prefixMatches(methodName)) {
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
            LuaType superType = getSuperClass(new SearchContext(project));
            if (superType != null)
                superType.addMethodCompletions(completionParameters, completionResultSet, false, true, useAsField);
        }
    }

    protected void addStaticMethodCompletions(@NotNull CompletionResultSet completionResultSet,
                                              boolean bold,
                                              boolean withSuper,
                                              SearchContext context) {
        Project project = getProject();
        if (project == null)
            return;
        String clazzName = getClassNameText();
        if (clazzName == null)
            return;
        Collection<LuaClassMethodDef> list = LuaClassMethodIndex.findStaticMethods(clazzName, context);
        for (LuaClassMethodDef def : list) {
            String methodName = def.getName();
            if (methodName != null && completionResultSet.getPrefixMatcher().prefixMatches(methodName)) {
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
            LuaType superType = getSuperClass(context);
            if (superType != null)
                superType.addStaticMethodCompletions(completionResultSet, false, true, context);
        }
    }

    public void addFieldCompletions(@NotNull CompletionParameters completionParameters,
                                    @NotNull CompletionResultSet completionResultSet,
                                    SearchContext context) {
        addFieldCompletions(completionParameters, completionResultSet, true, true, context);
        addStaticMethodCompletions(completionResultSet, true, true, context);
        addMethodCompletions(completionParameters, completionResultSet, true);
    }

    protected void addFieldCompletions(@NotNull CompletionParameters completionParameters,
                                       @NotNull CompletionResultSet completionResultSet,
                                       boolean bold,
                                       boolean withSuper,
                                       SearchContext context) {
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
            LuaType superType = getSuperClass(context);
            if (superType != null)
                superType.addFieldCompletions(completionParameters, completionResultSet, false, true, context);
        }
    }

    public LuaTypeSet guessFieldType(String propName, SearchContext context) {
        LuaTypeSet set = null;
        LuaClassField fieldDef = LuaClassFieldIndex.find(getClassNameText(), propName, context);
        if (fieldDef != null)
            set = fieldDef.guessType(context);
        else {
            LuaType superType = getSuperClass(context);
            if (superType != null)
                set = superType.guessFieldType(propName, context);
        }

        return set;
    }

    public LuaClassField findField(String fieldName, SearchContext context) {
        String className = getClassNameText();
        LuaClassField def = LuaClassFieldIndex.find(className, fieldName, context);
        if (def == null) {
            LuaType superType = getSuperClass(context);
            if (superType != null)
                def = superType.findField(fieldName, context);
        }
        return def;
    }

    public LuaClassMethodDef findMethod(String methodName, SearchContext context) {
        String className = getClassNameText();
        LuaClassMethodDef def = LuaClassMethodIndex.findMethodWithName(className, methodName, context);
        if (def == null) { // static
            def = LuaClassMethodIndex.findStaticMethod(className, methodName, context);
        }
        if (def == null) { // super
            LuaType superType = getSuperClass(context);
            if (superType != null)
                def = superType.findMethod(methodName, context);
        }
        return def;
    }

    public LuaClassMethodDef findStaticMethod(String methodName, boolean withSuper, @NotNull SearchContext context) {
        String className = getClassNameText();
        LuaClassMethodDef def = LuaClassMethodIndex.findStaticMethod(className, methodName, context);
        if (def == null && withSuper) {
            LuaType superType = getSuperClass(context);
            if (superType != null)
                def = superType.findStaticMethod(methodName, true, context);
        }
        return def;
    }
}
