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

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.Constants;
import com.tang.intellij.lua.comment.psi.LuaDocParamDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaGlobalFuncIndex;
import com.tang.intellij.lua.stubs.index.LuaGlobalVarIndex;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaPsiResolveUtil {

    private static LuaFuncBodyOwner funcBodyOwner = null;

    static LuaFuncBodyOwner resolveFuncBodyOwner(@NotNull LuaNameRef ref, SearchContext context) {
        String refName = ref.getName();
        //local 函数名
        if (funcBodyOwner == null) {
            LuaPsiTreeUtil.walkUpLocalFuncDef(ref, localFuncDef -> {
                if (refName.equals(localFuncDef.getName())) {
                    funcBodyOwner = localFuncDef;
                    return false;
                }
                return true;
            });
        }

        //global function
        if (funcBodyOwner == null) {
            funcBodyOwner = LuaGlobalFuncIndex.find(refName, context);
        }

        LuaFuncBodyOwner temp = funcBodyOwner;
        funcBodyOwner = null;
        return temp;
    }

    public static PsiElement resolveLocal(LuaNameRef ref, SearchContext context) {
        String refName = ref.getName();

        if (refName.equals(Constants.WORD_SELF)) {
            LuaClassMethodDef classMethodFuncDef = PsiTreeUtil.getParentOfType(ref, LuaClassMethodDef.class);
            if (classMethodFuncDef != null) {
                LuaNameRef nameRef = classMethodFuncDef.getClassMethodName().getNameRef();
                if (nameRef != null) {
                    PsiElement resolve = nameRef.resolve(context);
                    return resolve != null ? resolve : nameRef;
                }
            }
        }

        //local 变量, 参数
        LuaPsiTreeUtil.walkUpLocalNameDef(ref, nameDef -> {
            if (refName.equals(nameDef.getName())) {
                resolveResult = nameDef;
                return false;
            }
            return true;
        });

        //local 函数名
        if (resolveResult == null) {
            LuaPsiTreeUtil.walkUpLocalFuncDef(ref, nameDef -> {
                String name= nameDef.getName();
                if (refName.equals(name)) {
                    resolveResult = nameDef;
                    return false;
                }
                return true;
            });
        }

        PsiElement result = resolveResult;
        resolveResult = null;
        return result;
    }

    private static PsiElement resolveResult;

    /**
     * 查找这个引用
     * @param ref 要查找的ref
     * @param context context
     * @return PsiElement
     */
    public static PsiElement resolve(LuaNameRef ref, SearchContext context) {
        //search local
        resolveResult = resolveLocal(ref, context);

        String refName = ref.getName();
        //global field
        if (resolveResult == null) {
            LuaGlobalVar globalVar = LuaGlobalVarIndex.find(refName, context);
            if (globalVar != null) {
                resolveResult = globalVar.getNameRef();
            }
        }

        //global function
        if (resolveResult == null) {
            resolveResult = LuaGlobalFuncIndex.find(refName, context);
        }

        PsiElement result = resolveResult;
        resolveResult = null;
        return result;
    }

    public static PsiElement resolve(LuaIndexExpr indexExpr, SearchContext context) {
        PsiElement id = indexExpr.getId();
        if (id == null)
            return null;

        LuaTypeSet typeSet = indexExpr.guessPrefixType(context);
        if (typeSet != null) {
            String idString = id.getText();
            for (LuaType type : typeSet.getTypes()) {
                //属性
                LuaClassField fieldDef = type.findField(idString, context);
                if (fieldDef != null)
                    return fieldDef;
                //方法
                LuaClassMethodDef methodDef = type.findMethod(idString, context);
                if (methodDef != null)
                    return methodDef;
            }
        }
        return null;
    }

    static LuaTypeSet resolveType(LuaNameDef nameDef, SearchContext context) {
        //作为函数参数，类型在函数注释里找
        if (nameDef instanceof LuaParamNameDef) {
            LuaCommentOwner owner = PsiTreeUtil.getParentOfType(nameDef, LuaCommentOwner.class);
            if (owner != null) {
                LuaComment comment = owner.getComment();
                if (comment != null) {
                    LuaDocParamDef paramDef = comment.getParamDef(nameDef.getText());
                    if (paramDef != null) {
                        return paramDef.guessType(context);
                    }
                }
            }
        }
        //在Table字段里
        else if (nameDef.getParent() instanceof LuaTableField) {
            LuaTableField field = (LuaTableField) nameDef.getParent();
            LuaExpr expr = PsiTreeUtil.findChildOfType(field, LuaExpr.class);
            if (expr != null) return expr.guessType(context);
        }
        //变量声明，local x = 0
        else {
            LuaLocalDef localDef = PsiTreeUtil.getParentOfType(nameDef, LuaLocalDef.class);
            if (localDef != null) {
                LuaTypeSet typeSet = null;
                LuaComment comment = localDef.getComment();
                if (comment != null) {
                    typeSet = comment.guessType(context);
                }

                //计算 expr 返回类型
                if (typeSet == null || typeSet.isEmpty()) {
                    LuaNameList nameList = localDef.getNameList();
                    LuaExprList exprList = localDef.getExprList();
                    if (nameList != null && exprList != null) {
                        int index = nameList.getNameDefList().indexOf(nameDef);
                        if (index != -1) {
                            List<LuaExpr> exprs = exprList.getExprList();
                            if (index < exprs.size()) {
                                LuaExpr expr = exprs.get(index);
                                typeSet = expr.guessType(context);
                            }
                        }
                    }
                }

                //anonymous
                if (typeSet == null ||typeSet.isEmpty())
                    typeSet = LuaTypeSet.create(LuaType.createAnonymousType(nameDef));
                return typeSet;
            }
        }
        return null;
    }

    /**
     * 找到 require 的文件路径
     * @param pathString 参数字符串 require "aa.bb.cc"
     * @param project MyProject
     * @return PsiFile
     */
    public static LuaFile resolveRequireFile(String pathString, Project project) {
        if (pathString == null)
            return null;

        int lastDot = pathString.lastIndexOf('.');
        String packagePath = "";
        String fileName = pathString;
        if (lastDot != -1) {
            fileName = pathString.substring(lastDot + 1);
            packagePath = pathString.substring(0, lastDot);
        }
        fileName += ".lua";
        PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(packagePath);
        if (psiPackage != null) {
            PsiDirectory[] directories = psiPackage.getDirectories();
            for (PsiDirectory directory : directories) {
                PsiFile file = directory.findFile(fileName);
                if (file instanceof LuaFile)
                    return (LuaFile) file;
            }
        }
        return null;
    }

    public static String getAnonymousType(LuaNameDef nameDef) {
        String fileName = nameDef.getContainingFile().getName();
        int startOffset = nameDef.getNode().getStartOffset();

        return String.format("%s@(%d)%s", fileName, startOffset, nameDef.getName());
    }
}
