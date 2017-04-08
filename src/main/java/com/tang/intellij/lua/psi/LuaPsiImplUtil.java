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
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocClassNameRef;
import com.tang.intellij.lua.comment.psi.LuaDocParamDef;
import com.tang.intellij.lua.comment.psi.LuaDocReturnDef;
import com.tang.intellij.lua.comment.psi.LuaDocTypeSet;
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

    public static PsiReference[] getReferences(LuaPsiElement element) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element, PsiReferenceService.Hints.NO_HINTS);
    }

    public static PsiElement resolve(LuaNameExpr ref, SearchContext context) {
        return LuaPsiResolveUtil.resolve(ref, context);
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
        LuaNameExpr ref = classMethodDef.getClassMethodName().getNameRef();
        if (ref != null) {
            LuaTypeSet typeSet = ref.guessType(context);
            if (typeSet != null) {
                return typeSet.getFirst();
            }
        }
        return null;
    }

    public static LuaNameExpr getNameRef(LuaClassMethodName name) {
        return PsiTreeUtil.findChildOfType(name, LuaNameExpr.class);
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
        context = SearchContext.wrapDeadLock(context, SearchContext.TYPE_BODY_OWNER, callExpr);
        if (context.isDeadLock(1))
            return null;

        LuaExpr expr = callExpr.getExpr();
        if (expr instanceof LuaIndexExpr) {
            PsiElement resolve = LuaPsiResolveUtil.resolve((LuaIndexExpr) expr, context);
            if (resolve instanceof LuaFuncBodyOwner)
                return (LuaFuncBodyOwner) resolve;
        }
        else if (expr instanceof LuaNameExpr) {
            LuaNameExpr luaNameRef = (LuaNameExpr) expr;
            return LuaPsiResolveUtil.resolveFuncBodyOwner(luaNameRef, context);
        }
        return null;
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
                if (list.size() == 1 && list.get(0) instanceof LuaValueExpr) {
                    LuaValueExpr valueExpr = (LuaValueExpr) list.get(0);
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
        return LuaTypeSet.create();
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
        String signature = "(";

        for (int i = 0; i < params.length; i++) {
            LuaParamInfo info = params[i];
            if (info != null) {
                if (mask > 0) {
                    signature += ", " + info.getName();
                } else {
                    signature += info.getName();
                }
                mask = mask | (1 << i);
            }
        }

        signature += ")";
        processor.accept(signature, mask);
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
        return null;
    }

    public static String getFieldName(LuaTableField tableField) {
        LuaTableFieldStub stub = tableField.getStub();
        if (stub != null)
            return stub.getFieldName();
        return getName(tableField);
    }

    public static String toString(StubBasedPsiElement<? extends StubElement> stubElement) {
        return "[STUB]";// + stubElement.getNode().getElementType().toString();
    }

    public static LuaTypeSet guessType(LuaVar var, SearchContext context) {
        LuaExpr expr = var.getExpr();
        //TODO stack overflow
        if (expr instanceof LuaIndexExpr)
            return null;
        return expr.guessType(context);
    }

    public static LuaNameExpr getNameRef(LuaVar var) {
        return PsiTreeUtil.getChildOfType(var, LuaNameExpr.class);
    }

    public static String getFieldName(LuaVar var) {
        LuaVarStub stub = var.getStub();
        if (stub != null)
            return stub.getFieldName();

        LuaExpr expr = var.getExpr();
        if (expr instanceof LuaIndexExpr) {
            LuaIndexExpr luaIndexExpr = (LuaIndexExpr) expr;
            PsiElement id = luaIndexExpr.getId();
            if (id != null)
                return id.getText();
        }
        return null;
    }

    public static String getName(LuaVar var) {
        LuaVarStub stub = var.getStub();
        if (stub != null)
            return stub.getFieldName();
        return getName((PsiNameIdentifierOwner) var);
    }

    public static ItemPresentation getPresentation(LuaVar var) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return var.getName();
            }

            @NotNull
            @Override
            public String getLocationString() {
                return var.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return LuaIcons.CLASS_FIELD;
            }
        };
    }

    public static PsiElement getNameIdentifier(LuaVar var) {
        LuaExpr expr = var.getExpr();
        if (expr instanceof LuaIndexExpr) {
            return ((LuaIndexExpr) expr).getId();
        } else if (var.getNameRef() != null) {
            return var.getNameRef().getId();
        }
        return null;
    }

    @NotNull
    public static PsiElement getNameIdentifier(LuaNameExpr ref) {
        return ref.getFirstChild();
    }

    @NotNull
    public static String getName(LuaNameExpr ref) {
        return ref.getText();
    }
}
