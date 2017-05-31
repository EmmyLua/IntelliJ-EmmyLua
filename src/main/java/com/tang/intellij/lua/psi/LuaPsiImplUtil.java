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

package com.tang.intellij.lua.psi;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * LuaPsiImplUtil
 * Created by TangZX on 2016/11/22.
 */
public class LuaPsiImplUtil {

    public static PsiElement setName(LuaNamedElement identifier, String name) {
        PsiElement newId = LuaElementFactory.createIdentifier(identifier.getProject(), name);
        PsiElement oldId = identifier.getFirstChild();
        oldId.replace(newId);
        return newId;
    }

    public static PsiElement setName(PsiNameIdentifierOwner owner, String name) {
        PsiElement oldId = owner.getNameIdentifier();
        if (oldId != null) {
            PsiElement newId = LuaElementFactory.createIdentifier(owner.getProject(), name);
            oldId.replace(newId);
            return newId;
        }
        return owner;
    }

    @NotNull
    public static String getName(LuaNamedElement identifier) {
        return identifier.getText();
    }

    public static LuaTypeSet guessType(LuaNameDef nameDef, SearchContext context) {
        return LuaPsiResolveUtil.resolveType(nameDef, context);
    }

    public static PsiElement getNameIdentifier(LuaNameDef nameDef) {
        return nameDef.getFirstChild();
    }

    /**
     * LuaNameDef 只可能在本文件中搜
     * @param nameDef def
     * @return SearchScope
     */
    public static SearchScope getUseScope(LuaNameDef nameDef) {
        return GlobalSearchScope.fileScope(nameDef.getContainingFile());
    }

    @NotNull
    public static PsiReference[] getReferences(LuaPsiElement element) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element, PsiReferenceService.Hints.NO_HINTS);
    }

    /**
     * 寻找 Comment
     * @param declaration owner
     * @return LuaComment
     */
    public static LuaComment getComment(LuaCommentOwner declaration) {
        return LuaCommentUtil.findComment(declaration);
    }

    public static PsiElement getNameIdentifier(LuaClassMethodDef classMethodDef) {
        return classMethodDef.getClassMethodName().getId();
    }

    public static String getName(LuaClassMethodDef classMethodDef) {
        LuaClassMethodStub stub = classMethodDef.getStub();
        if (stub != null)
            return stub.getShortName();
        return getName((PsiNameIdentifierOwner)classMethodDef);
    }

    public static boolean isStatic(LuaClassMethodDef classMethodDef) {
        LuaClassMethodStub stub = classMethodDef.getStub();
        if (stub != null)
            return stub.isStatic();

        return classMethodDef.getClassMethodName().getDot() != null;
    }

    public static ItemPresentation getPresentation(LuaClassMethodDef classMethodDef) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return classMethodDef.getName();
            }

            @NotNull
            @Override
            public String getLocationString() {
                return classMethodDef.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.CLASS_METHOD;
            }
        };
    }

    /**
     * 寻找对应的类
     * @param classMethodDef def
     * @return LuaType
     */
    @Nullable
    public static LuaType getClassType(LuaClassMethodDef classMethodDef, SearchContext context) {
        LuaExpr expr = classMethodDef.getClassMethodName().getExpr();
        LuaTypeSet typeSet = expr.guessType(context);
        if (typeSet != null) {
            return typeSet.getPerfect();
        }
        return null;
    }

    public static PsiElement getNameIdentifier(LuaGlobalFuncDef globalFuncDef) {
        return globalFuncDef.getId();
    }

    public static String getName(LuaGlobalFuncDef globalFuncDef) {
        LuaGlobalFuncStub stub = globalFuncDef.getStub();
        if (stub != null)
            return stub.getName();
        return getName((PsiNameIdentifierOwner)globalFuncDef);
    }

    public static ItemPresentation getPresentation(LuaGlobalFuncDef globalFuncDef) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return globalFuncDef.getName();
            }

            @NotNull
            @Override
            public String getLocationString() {
                return globalFuncDef.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return AllIcons.Nodes.Function;
            }
        };
    }

    /**
     * 猜出前面的类型
     * @param callExpr call expr
     * @return LuaTypeSet
     */
    public static LuaTypeSet guessPrefixType(LuaCallExpr callExpr, SearchContext context) {
        LuaExpr prefix = (LuaExpr) callExpr.getFirstChild();
        if (prefix != null)
            return prefix.guessType(context);
        return null;
    }

    /**
     * 找出函数体
     * @param callExpr call expr
     * @return LuaFuncBodyOwner
     */
    public static LuaFuncBodyOwner resolveFuncBodyOwner(LuaCallExpr callExpr, SearchContext context) {
        LuaFuncBodyOwner owner = null;
        if (context.push(callExpr, SearchContext.Overflow.FindBodyOwner)) {
            LuaExpr expr = callExpr.getExpr();
            if (expr instanceof LuaIndexExpr) {
                PsiElement resolve = LuaPsiResolveUtil.resolve((LuaIndexExpr) expr, context);
                if (resolve instanceof LuaFuncBodyOwner)
                    owner = (LuaFuncBodyOwner) resolve;
            } else if (expr instanceof LuaNameExpr) {
                LuaNameExpr luaNameRef = (LuaNameExpr) expr;
                owner = LuaPsiResolveUtil.resolveFuncBodyOwner(luaNameRef, context);
            }
            context.pop(callExpr);
        }
        return owner;
    }

    /**
     * 获取第一个字符串参数
     * @param callExpr callExpr
     * @return String PsiElement
     */
    public static PsiElement getFirstStringArg(LuaCallExpr callExpr) {
        LuaArgs args = callExpr.getArgs();
        PsiElement path = null;

        // require "xxx"
        for (PsiElement child = args.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNode().getElementType() == LuaTypes.STRING) {
                path = child;
                break;
            }
        }
        // require("")
        if (path == null) {
            LuaExprList exprList = args.getExprList();
            if (exprList != null) {
                List<LuaExpr> list = exprList.getExprList();
                if (list.size() == 1 && list.get(0) instanceof LuaLiteralExpr) {
                    LuaLiteralExpr valueExpr = (LuaLiteralExpr) list.get(0);
                    PsiElement node = valueExpr.getFirstChild();
                    if (node.getNode().getElementType() == LuaTypes.STRING) {
                        path = node;
                    }
                }
            }
        }
        return path;
    }

    public static boolean isStaticMethodCall(LuaCallExpr callExpr) {
        LuaExpr expr = callExpr.getExpr();
        return expr instanceof LuaIndexExpr && ((LuaIndexExpr) expr).getColon() == null;
    }

    public static boolean isMethodCall(LuaCallExpr callExpr) {
        LuaExpr expr = callExpr.getExpr();
        return expr instanceof LuaIndexExpr && ((LuaIndexExpr) expr).getColon() != null;
    }

    public static boolean isFunctionCall(LuaCallExpr callExpr) {
        return callExpr.getExpr() instanceof LuaNameExpr;
    }

    public static LuaTypeSet guessTypeAt(LuaExprList list, int index, SearchContext context) {
        int cur = 0;
        for (PsiElement child = list.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof LuaExpr) {
                if (cur == index) {
                    return ((LuaExpr)child).guessType(context);
                }
                cur++;
            }
        }
        return null;
    }

    public static LuaTypeSet guessPrefixType(LuaIndexExpr indexExpr, SearchContext context) {
        LuaExpr prefix = (LuaExpr) indexExpr.getFirstChild();
        if (prefix != null)
            return prefix.guessType(context);
        return null;
    }

    @Nullable
    public static PsiElement getNameIdentifier(LuaIndexExpr indexExpr) {
        return indexExpr.getId();
    }

    public static ItemPresentation getPresentation(LuaIndexExpr indexExpr) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return indexExpr.getName();
            }

            @NotNull
            @Override
            public String getLocationString() {
                return indexExpr.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.CLASS_FIELD;
            }
        };
    }

    public static String getName(LuaIndexExpr indexExpr) {
        LuaIndexStub stub = indexExpr.getStub();
        if (stub != null)
            return stub.getFieldName();
        return getName((PsiNameIdentifierOwner)indexExpr);
    }

    public static LuaTypeSet guessValueType(LuaIndexExpr indexExpr, SearchContext context) {
        LuaIndexStub stub = indexExpr.getStub();
        if (stub != null) {
            return stub.guessValueType();
        }

        Optional<LuaTypeSet> setOptional = Optional.of(indexExpr)
                .filter(s -> s.getParent() instanceof LuaVar)
                .map(PsiElement::getParent)
                .filter(s -> s.getParent() instanceof LuaVarList)
                .map(PsiElement::getParent)
                .filter(s -> s.getParent() instanceof LuaAssignStat)
                .map(PsiElement::getParent)
                .map(s -> {
                    LuaAssignStat assignStat = (LuaAssignStat) s;
                    LuaExprList exprList = assignStat.getExprList();
                    if (exprList != null) {
                        return exprList.guessTypeAt(0, context);
                    }
                    return null;
                });
        return setOptional.orElse(null);
    }

    public static LuaTableField findField(LuaTableConstructor table, String fieldName) {
        LuaFieldList fieldList = table.getFieldList();
        if (fieldList != null) {
            for (LuaTableField field : fieldList.getTableFieldList()) {
                if (fieldName.equals(field.getName()))
                    return field;
            }
        }
        return null;
    }

    public static List<LuaParamNameDef> getParamNameDefList(LuaFuncBodyOwner funcBodyOwner) {
        LuaFuncBody funcBody = funcBodyOwner.getFuncBody();
        if (funcBody != null)
            return funcBody.getParamNameDefList();
        else
            return null;
    }

    public static List<LuaParamNameDef> getParamNameDefList(LuaForAStat forAStat) {
        List<LuaParamNameDef> list = new ArrayList<>();
        list.add(forAStat.getParamNameDef());
        return list;
    }

    public static LuaTypeSet guessReturnTypeSet(LuaFuncBodyOwner owner, SearchContext searchContext) {
        if (owner instanceof StubBasedPsiElementBase) {
            StubBasedPsiElementBase stubElement = (StubBasedPsiElementBase) owner;
            StubElement stub = stubElement.getStub();
            if (stub instanceof LuaFuncBodyOwnerStub) {
                LuaFuncBodyOwnerStub funcBodyOwnerStub = (LuaFuncBodyOwnerStub) stub;
                return funcBodyOwnerStub.getReturnTypeSet();
            }
        }

        return guessReturnTypeSetOriginal(owner, searchContext);
    }

    private static final Key<CachedValue<LuaTypeSet>> FUNCTION_RETURN_TYPESET = Key.create("lua.function.return_typeset");

    @NotNull
    public static LuaTypeSet guessReturnTypeSetOriginal(LuaFuncBodyOwner owner, SearchContext searchContext) {
        if (owner instanceof LuaCommentOwner) {
            LuaComment comment = LuaCommentUtil.findComment((LuaCommentOwner) owner);
            if (comment != null) {
                LuaDocReturnDef returnDef = PsiTreeUtil.findChildOfType(comment, LuaDocReturnDef.class);
                if (returnDef != null) {
                    return returnDef.resolveTypeAt(0, searchContext); //TODO : multi
                }
            }
        }

        //infer from return stat
        return CachedValuesManager.getManager(owner.getProject()).getCachedValue(owner, FUNCTION_RETURN_TYPESET, () -> {
            LuaTypeSet typeSet = LuaTypeSet.create();
            Ref<LuaTypeSet> setRef = Ref.create(typeSet);
            owner.acceptChildren(new LuaVisitor() {
                @Override
                public void visitReturnStat(@NotNull LuaReturnStat o) {
                    LuaTypeSet set = setRef.get();
                    setRef.set(set.union(guessReturnTypeSet(o, 0, searchContext)));
                }

                @Override
                public void visitFuncBodyOwner(@NotNull LuaFuncBodyOwner o) {
                    // ignore sub function
                }

                @Override
                public void visitPsiElement(@NotNull LuaPsiElement o) {
                    o.acceptChildren(this);
                }
            });
            return CachedValueProvider.Result.create(setRef.get(), owner);
        }, false);
    }

    @NotNull
    public static LuaParamInfo[] getParams(LuaFuncBodyOwner owner) {
        if (owner instanceof StubBasedPsiElementBase) {
            StubBasedPsiElementBase stubElement = (StubBasedPsiElementBase) owner;
            StubElement stub = stubElement.getStub();
            if (stub instanceof LuaFuncBodyOwnerStub) {
                LuaFuncBodyOwnerStub funcBodyOwnerStub = (LuaFuncBodyOwnerStub) stub;
                return funcBodyOwnerStub.getParams();
            }
        }
        return getParamsOriginal(owner);
    }

    @NotNull
    public static LuaParamInfo[] getParamsOriginal(LuaFuncBodyOwner funcBodyOwner) {
        LuaComment comment = null;
        if (funcBodyOwner instanceof LuaCommentOwner) {
            comment = LuaCommentUtil.findComment((LuaCommentOwner) funcBodyOwner);
        }

        List<LuaParamNameDef> paramNameList = funcBodyOwner.getParamNameDefList();
        if (paramNameList != null) {
            LuaParamInfo[] array = new LuaParamInfo[paramNameList.size()];
            for (int i = 0; i < paramNameList.size(); i++) {
                LuaParamInfo paramInfo = new LuaParamInfo();
                String paramName = paramNameList.get(i).getText();
                paramInfo.setName(paramName);
                // param types
                if (comment != null) {
                    LuaDocParamDef paramDef = comment.getParamDef(paramName);
                    if (paramDef != null) {
                        paramInfo.setOptional(paramDef.getOptional() != null);
                        LuaDocTypeSet luaDocTypeSet = paramDef.getTypeSet();
                        if (luaDocTypeSet != null) {
                            List<LuaDocClassNameRef> classNameRefList = luaDocTypeSet.getClassNameRefList();
                            String[] types = new String[classNameRefList.size()];
                            for (int j = 0; j < classNameRefList.size(); j++) {
                                types[j] = classNameRefList.get(j).getText();
                            }
                            paramInfo.setTypes(types);
                        }
                    }
                }
                array[i] = paramInfo;
            }
            return array;
        }
        return new LuaParamInfo[0];
    }

    static String getParamSignature(LuaFuncBodyOwner funcBodyOwner) {
        LuaParamInfo[] params = funcBodyOwner.getParams();
        String[] list = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            LuaParamInfo lpi = params[i];
            String s = lpi.getName();
            if (lpi.isOptional())
                s = "[" + s + "]";
            list[i] = s;
        }
        return "(" + String.join(", ", list) + ")";
    }

    public interface OptionalFuncProcessor {
        void accept(String signature, int mask);
    }

    private static HashSet<Integer> sets = new HashSet<>();
    public static void processOptional(LuaParamInfo[] params, OptionalFuncProcessor processor) {
        sets.clear();
        processOptionalFunc(params, processor);
    }
    private static void processOptionalFunc(LuaParamInfo[] params, OptionalFuncProcessor processor) {
        int mask = 0;
        StringBuilder signature = new StringBuilder("(");

        for (int i = 0; i < params.length; i++) {
            LuaParamInfo info = params[i];
            if (info != null) {
                if (mask > 0) {
                    signature.append(", ").append(info.getName());
                } else {
                    signature.append(info.getName());
                }
                mask = mask | (1 << i);
            }
        }

        signature.append(")");
        processor.accept(signature.toString(), mask);
        sets.add(mask);
        for (int i = 0; i < params.length; i++) {
            LuaParamInfo info = params[i];
            if (info != null && info.isOptional()) {
                int s = mask ^ (1 << i);
                if (!sets.contains(s)) {
                    params[i] = null;
                    processOptionalFunc(params, processor);
                    params[i] = info;
                }
            }
        }
    }

    public static PsiElement getNameIdentifier(LuaLocalFuncDef localFuncDef) {
        return localFuncDef.getId();
    }

    public static SearchScope getUseScope(LuaLocalFuncDef localFuncDef) {
        return GlobalSearchScope.fileScope(localFuncDef.getContainingFile());
    }

    public static String getName(PsiNameIdentifierOwner identifierOwner) {
        PsiElement id = identifierOwner.getNameIdentifier();
        return id != null ? id.getText() : null;
    }

    public static int getTextOffset(PsiNameIdentifierOwner localFuncDef) {
        PsiElement id = localFuncDef.getNameIdentifier();
        if (id != null) return id.getTextOffset();
        return localFuncDef.getNode().getStartOffset();
    }

    public static PsiElement getNameIdentifier(LuaTableField tableField) {
        return tableField.getId();
    }

    public static LuaTypeSet guessType(LuaTableField tableField, SearchContext context) {
        PsiElement lastChild = tableField.getLastChild();
        if (lastChild instanceof LuaExpr) {
            return ((LuaExpr)lastChild).guessType(context);
        }
        return null;
    }

    public static String getFieldName(LuaTableField tableField) {
        LuaTableFieldStub stub = tableField.getStub();
        if (stub != null)
            return stub.getFieldName();
        return getName(tableField);
    }

    public static ItemPresentation getPresentation(LuaTableField tableField) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return tableField.getName();
            }

            @NotNull
            @Override
            public String getLocationString() {
                return tableField.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.CLASS_FIELD;
            }
        };
    }

    public static String toString(StubBasedPsiElement<? extends StubElement> stubElement) {
        return "STUB:[" + stubElement.getClass().getSimpleName() + "]";
    }

    public static LuaTypeSet guessType(LuaVar var, SearchContext context) {
        LuaExpr expr = var.getExpr();
        //TODO stack overflow
        if (expr instanceof LuaIndexExpr)
            return null;
        return expr.guessType(context);
    }

    public static ItemPresentation getPresentation(LuaNameExpr nameExpr) {
        return new ItemPresentation() {
            @NotNull
            @Override
            public String getPresentableText() {
                return nameExpr.getName();
            }

            @NotNull
            @Override
            public String getLocationString() {
                return nameExpr.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.CLASS_FIELD;
            }
        };
    }

    @NotNull
    public static PsiElement getNameIdentifier(LuaNameExpr ref) {
        return ref.getId();
    }

    @NotNull
    public static String getName(LuaNameExpr nameExpr) {
        LuaNameStub stub = nameExpr.getStub();
        if (stub != null)
            return stub.getName();
        return nameExpr.getText();
    }

    /**
     * 找出 LuaAssignStat 的第 index 位置上的 Var 的类型String
     * @param stat LuaAssignStat
     * @param index index
     * @return type string
     * todo 处理多个var的情况
     */
    public static String getTypeName(LuaAssignStat stat, int index) {
        LuaVarList varList = stat.getVarList();
        LuaVar luaVar = varList.getVarList().get(index);
        String typeName = null;
        if (luaVar != null && luaVar.getExpr() instanceof LuaNameExpr) {
            // common 优先
            LuaComment comment = stat.getComment();
            if (comment != null) {
                LuaDocClassDef classDef = comment.getClassDef();
                if (classDef != null) {
                    typeName = classDef.getName();
                }
            }
            // 否则直接当成Global，以名字作类型
            if (typeName == null)
                typeName = luaVar.getText();
        }
        return typeName;
    }

    public static LuaTypeSet guessReturnTypeSet(LuaReturnStat returnStat, int index, SearchContext context) {
        if (returnStat != null) {
            LuaExprList returnExpr = returnStat.getExprList();
            if (returnExpr != null)
                return returnExpr.guessTypeAt(index, context);
        }
        return null;
    }
}
