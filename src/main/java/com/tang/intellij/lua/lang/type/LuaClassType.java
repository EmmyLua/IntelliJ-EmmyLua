package com.tang.intellij.lua.lang.type;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.comment.psi.LuaDocFieldNameDef;
import com.tang.intellij.lua.editor.completion.FuncInsertHandler;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * class type
 * Created by tangzx on 2016/12/21.
 */
public class LuaClassType extends LuaType {

    public static LuaClassType create(@NotNull LuaDocClassDef def) {
        LuaClassType type = new LuaClassType();
        type.classDef = def;
        return type;
    }

    private LuaDocClassDef classDef;
    private String className;
    private LuaType superType;

    public LuaType getSuperClass() {
        if (superType == null)
            superType = classDef.getSuperClass();
        return superType;
    }

    public String getClassNameText() {
        if (className == null)
            className = classDef.getClassNameText();
        return className;
    }

    protected void addMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean bold, boolean withSuper) {
        String clazzName = getClassNameText();
        Collection<LuaClassMethodDef> list = LuaClassMethodIndex.getInstance().get(clazzName, classDef.getProject(), new ProjectAndLibrariesScope(classDef.getProject()));
        for (LuaClassMethodDef def : list) {
            LookupElementBuilder elementBuilder = LookupElementBuilder.create(def.getMethodName())
                    .withIcon(LuaIcons.CLASS_METHOD)
                    .withInsertHandler(new FuncInsertHandler(def.getFuncBody()))
                    .withTypeText(clazzName);
            if (bold)
                elementBuilder = elementBuilder.bold();

            completionResultSet.addElement(elementBuilder);
        }

        if (withSuper) {
            LuaType superType = getSuperClass();
            if (superType != null)
                superType.addMethodCompletions(completionParameters, completionResultSet, false, true);
        }
    }

    protected void addStaticMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean bold, boolean withSuper) {
        String clazzName = getClassNameText();
        Collection<LuaClassMethodDef> list = LuaClassMethodIndex.findStaticMethods(clazzName, classDef.getProject(), new ProjectAndLibrariesScope(classDef.getProject()));
        for (LuaClassMethodDef def : list) {
            LookupElementBuilder elementBuilder = LookupElementBuilder.create(def.getMethodName())
                    .withIcon(LuaIcons.CLASS_METHOD)
                    .withInsertHandler(new FuncInsertHandler(def.getFuncBody()))
                    .withTypeText(clazzName)
                    .withItemTextUnderlined(true);
            if (bold)
                elementBuilder = elementBuilder.bold();

            completionResultSet.addElement(elementBuilder);
        }

        if (withSuper) {
            LuaType superType = getSuperClass();
            if (superType != null)
                superType.addStaticMethodCompletions(completionParameters, completionResultSet, false, true);
        }
    }

    protected void addFieldCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean bold, boolean withSuper) {
        String clazzName = getClassNameText();
        Collection<LuaDocFieldDef> list = LuaClassFieldIndex.getInstance().get(clazzName, classDef.getProject(), new ProjectAndLibrariesScope(classDef.getProject()));

        for (LuaDocFieldDef fieldName : list) {
            LuaDocFieldNameDef nameDef = fieldName.getFieldNameDef();
            if (nameDef == null)
                continue;

            LookupElementBuilder elementBuilder = LookupElementBuilder.create(nameDef.getName())
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
        LuaDocFieldDef fieldDef = LuaClassFieldIndex.find(getClassNameText(), propName, project, scope);
        if (fieldDef != null)
            set = fieldDef.resolveType();
        else {
            LuaType superType = getSuperClass();
            if (superType != null)
                set = superType.guessFieldType(propName, project, scope);
        }

        return set;
    }

    public LuaDocFieldDef findField(String fieldName, Project project, GlobalSearchScope scope) {
        String className = getClassNameText();
        LuaDocFieldDef def = LuaClassFieldIndex.find(className, fieldName, project, scope);
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
