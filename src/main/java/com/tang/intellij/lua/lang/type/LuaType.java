/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.lang.type;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaNameExpr;
import com.tang.intellij.lua.psi.LuaPsiResolveUtil;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaClassIndex;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;

/**
 * 类型说明
 * Created by TangZX on 2016/12/4.
 */
public class LuaType implements Comparable<LuaType> {

    public static LuaType create(@NotNull String typeName, @Nullable String superTypeName) {
        LuaType type = new LuaType();
        type.clazzName = typeName;
        type.superClassName = superTypeName;
        return type;
    }

    public static LuaType createAnonymousType(PsiElement element) {
        LuaType type = create(LuaPsiResolveUtil.getAnonymousType(element), null);
        type.isAnonymous = true;
        return type;
    }

    public static LuaType createGlobalType(LuaNameExpr ref) {
        return create(ref.getText(), null);
    }

    protected LuaType() {
    }

    // 模糊匹配的结果
    private boolean isUnreliable;
    boolean isAnonymous;
    String clazzName;
    private String aliasName;
    private String superClassName;

    public void initAliasName(SearchContext context) {
        if (aliasName == null) {
            LuaDocClassDef classDef = LuaClassIndex.find(clazzName, context);
            if (classDef != null) {
                LuaType classType = classDef.getClassType();
                setAliasName(classType.getAliasName());
            }
        }
    }

    public LuaType getSuperClass(SearchContext context) {
        if (superClassName != null) {
            LuaDocClassDef superClassDef = LuaClassIndex.find(superClassName, context);
            if (superClassDef != null)
                return superClassDef.getClassType();
        }
        return null;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public String getClassName() {
        return clazzName;
    }

    public String getDisplayName() {
        return isAnonymous() ? "" : getClassName();
    }

    void serialize(@NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeBoolean(isAnonymous);
        stubOutputStream.writeName(clazzName);
        stubOutputStream.writeName(aliasName);
        stubOutputStream.writeName(superClassName);
    }

    void deserialize(@NotNull StubInputStream stubInputStream) throws IOException {
        isAnonymous = stubInputStream.readBoolean();
        clazzName = StringRef.toString(stubInputStream.readName());
        aliasName = StringRef.toString(stubInputStream.readName());
        superClassName = StringRef.toString(stubInputStream.readName());
    }

    @Override
    public int compareTo(@NotNull LuaType other) {
        return other.clazzName.compareTo(this.clazzName);
    }

    public interface Processor<T> {
        void process(LuaType type, T t);
    }

    public void processFields(@NotNull SearchContext context,
                              Processor<LuaClassField> processor) {
        String clazzName = getClassName();
        if (clazzName == null)
            return;
        Project project = context.getProject();
        assert project != null;

        LuaClassFieldIndex fieldIndex = LuaClassFieldIndex.getInstance();
        Collection<LuaClassField> list = fieldIndex.get(clazzName, project, new ProjectAndLibrariesScope(project));
        if (aliasName != null) {
            Collection<LuaClassField> classFields = fieldIndex.get(aliasName, project, new ProjectAndLibrariesScope(project));
            list.addAll(classFields);
        }

        for (LuaClassField field : list) {
            processor.process(this, field);
        }

        // super
        LuaType superType = getSuperClass(context);
        if (superType != null)
            superType.processFields(context, processor);
    }

    public void processMethods(@NotNull SearchContext context,
                               Processor<LuaClassMethodDef> processor) {
        String clazzName = getClassName();
        if (clazzName == null)
            return;
        Project project = context.getProject();
        assert project != null;

        LuaClassMethodIndex methodIndex = LuaClassMethodIndex.getInstance();
        Collection<LuaClassMethodDef> list = methodIndex.get(clazzName, project, new ProjectAndLibrariesScope(project));
        if (aliasName != null) {
            list.addAll(methodIndex.get(aliasName, project, new ProjectAndLibrariesScope(project)));
        }
        for (LuaClassMethodDef def : list) {
            String methodName = def.getName();
            if (methodName != null) {
                processor.process(this, def);
            }
        }

        LuaType superType = getSuperClass(context);
        if (superType != null)
            superType.processMethods(context, processor);
    }

    public void processStaticMethods(@NotNull SearchContext context,
                                     Processor<LuaClassMethodDef> processor) {
        String clazzName = getClassName();
        if (clazzName == null)
            return;
        Collection<LuaClassMethodDef> list = LuaClassMethodIndex.findStaticMethods(clazzName, context);
        if (aliasName != null) {
            list.addAll(LuaClassMethodIndex.findStaticMethods(aliasName, context));
        }
        for (LuaClassMethodDef def : list) {
            String methodName = def.getName();
            if (methodName != null) {
                processor.process(this, def);
            }
        }

        LuaType superType = getSuperClass(context);
        if (superType != null)
            superType.processStaticMethods(context, processor);
    }

    public LuaClassField findField(String fieldName, SearchContext context) {
        LuaClassField def = LuaClassFieldIndex.find(this, fieldName, context);
        if (def == null) {
            LuaType superType = getSuperClass(context);
            if (superType != null)
                def = superType.findField(fieldName, context);
        }
        return def;
    }

    @Nullable
    public LuaClassMethodDef findMethod(String methodName, SearchContext context) {
        String className = getClassName();
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

    public boolean isUnreliable() {
        return isUnreliable;
    }

    public void setUnreliable(boolean unreliable) {
        isUnreliable = unreliable;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    /*private LuaClassMethodDef findStaticMethod(String methodName, @NotNull SearchContext context) {
        String className = getClassName();
        LuaClassMethodDef def = LuaClassMethodIndex.findStaticMethod(className, methodName, context);
        if (def == null) {
            LuaType superType = getSuperClass(context);
            if (superType != null)
                def = superType.findStaticMethod(methodName, context);
        }
        return def;
    }*/
}
