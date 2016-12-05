package com.tang.intellij.lua.lang.type;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.psi.LuaClassMethodFuncDef;
import com.tang.intellij.lua.psi.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 类型说明
 * Created by TangZX on 2016/12/4.
 */
public class LuaType {

    public static LuaType create(LuaDocClassDef classDef) {
        LuaType type = new LuaType();
        type.classDef = classDef;
        return type;
    }

    protected LuaType() {

    }

    private LuaDocClassDef classDef;

    public LuaDocClassDef getClassDef() {
        return classDef;
    }

    public String getClassNameText() {
        return classDef.getClassNameText();
    }

    public void addMethodCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {
        if (classDef != null) {
            String clazzName = getClassNameText();
            Collection<LuaClassMethodFuncDef> list = LuaClassMethodIndex.getInstance().get(clazzName, classDef.getProject(), new ProjectAndLibrariesScope(classDef.getProject()));
            for (LuaClassMethodFuncDef def : list) {
                //取名字的后半截 : 之后的部分
                PsiElement postfixName = def.getClassMethodName().getId();

                LookupElementBuilder elementBuilder = LookupElementBuilder.create(postfixName.getText())
                        .withIcon(AllIcons.Nodes.Method)
                        .withTypeText(clazzName);

                completionResultSet.addElement(elementBuilder);
            }
        }
    }

    public void addFieldCompletions(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {

    }
}
