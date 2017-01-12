package com.tang.intellij.lua.lang.type;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.editor.completion.FuncInsertHandler;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaNameDef;
import com.tang.intellij.lua.psi.LuaPsiResolveUtil;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 *
 * Created by tangzx on 2017/1/12.
 */
public class LuaAnonymousType extends LuaType {

    private final String clazzName;
    private LuaNameDef localDef;

    private LuaAnonymousType(LuaNameDef localDef) {
        this.localDef = localDef;
        this.clazzName = LuaPsiResolveUtil.getAnonymousType(localDef);
    }

    @Override
    public String getClassNameText() {
        return clazzName;
    }

    public static LuaAnonymousType create(LuaNameDef localDef) {
        return new LuaAnonymousType(localDef);
    }

    @Override
    protected void addMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean bold, boolean withSuper, boolean useAsField) {
        String clazzName = getClassNameText();
        Collection<LuaClassMethodDef> list = LuaClassMethodIndex.getInstance().get(clazzName, localDef.getProject(), new ProjectAndLibrariesScope(localDef.getProject()));
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
    }

    protected void addFieldCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, boolean bold, boolean withSuper) {
        String clazzName = getClassNameText();
        Collection<LuaClassField> list = LuaClassFieldIndex.getInstance().get(clazzName, localDef.getProject(), new ProjectAndLibrariesScope(localDef.getProject()));

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
}
