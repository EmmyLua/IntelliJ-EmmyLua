package com.tang.intellij.lua.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocGlobalDef;
import com.tang.intellij.lua.comment.psi.LuaDocParamDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.stubs.index.LuaGlobalFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaGlobalFuncIndex;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaPsiResolveUtil {

    private static LuaFuncBodyOwner funcBodyOwner = null;

    static LuaFuncBodyOwner resolveFuncBodyOwner(@NotNull LuaNameRef ref) {
        String refName = ref.getName();
        //local 函数名
        if (funcBodyOwner == null) {
            LuaPsiTreeUtil.walkUpLocalFuncDef(ref, localFuncDef -> {
                LuaName nameDef = localFuncDef.getLocalFuncNameDef();
                if (nameDef != null && refName.equals(nameDef.getName())) {
                    funcBodyOwner = localFuncDef;
                    return false;
                }
                return true;
            });
        }

        //global function
        if (funcBodyOwner == null) {
            Project project = ref.getProject();
            funcBodyOwner = LuaGlobalFuncIndex.find(refName, project, new ProjectAndLibrariesScope(project));
        }

        LuaFuncBodyOwner temp = funcBodyOwner;
        funcBodyOwner = null;
        return temp;
    }

    private static PsiElement resolveResult;

    public static PsiElement resolve(LuaNameRef ref) {
        String refName = ref.getName();

        if (refName.equals("self")) {
            LuaClassMethodDef classMethodFuncDef = PsiTreeUtil.getParentOfType(ref, LuaClassMethodDef.class);
            if (classMethodFuncDef != null) {
                LuaNameRef nameRef = classMethodFuncDef.getClassMethodName().getNameRef();
                if (nameRef != null)
                    return nameRef.resolve();
            }
            return null;
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
            LuaPsiTreeUtil.walkUpLocalFuncNameDef(ref, nameDef -> {
                if (refName.equals(nameDef.getName())) {
                    resolveResult = nameDef;
                    return false;
                }
                return true;
            });
        }

        //global field
        if (resolveResult == null) {
            Project project = ref.getProject();
            LuaDocGlobalDef globalDef = LuaGlobalFieldIndex.find(refName, project, new ProjectAndLibrariesScope(project));
            if (globalDef != null) {
                LuaCommentOwner owner = LuaCommentUtil.findOwner(globalDef);
                if (owner instanceof LuaAssignStat) {
                    LuaAssignStat assignStat = (LuaAssignStat) owner;
                    List<LuaVar> varList = assignStat.getVarList().getVarList();
                    for (LuaVar var : varList) {
                        LuaNameRef nameRef = var.getNameRef();
                        if (nameRef != null && nameRef.getText().equals(refName)) {
                            resolveResult = nameRef;
                            break;
                        }
                    }
                }
            }
        }

        //global function
        if (resolveResult == null) {
            Project project = ref.getProject();
            resolveResult = LuaGlobalFuncIndex.find(refName, project, new ProjectAndLibrariesScope(project));
        }

        PsiElement result = resolveResult;
        resolveResult = null;
        return result;
    }

    static LuaTypeSet resolveType(LuaNameDef nameDef) {
        //作为函数参数，类型在函数注释里找
        if (nameDef instanceof LuaParamNameDef) {
            LuaCommentOwner owner = PsiTreeUtil.getParentOfType(nameDef, LuaCommentOwner.class);
            if (owner != null) {
                LuaComment comment = owner.getComment();
                if (comment != null) {
                    LuaDocParamDef paramDef = comment.getParamDef(nameDef.getText());
                    if (paramDef != null) {
                        return paramDef.resolveType();
                    }
                }
            }
        }
        //在Table字段里
        else if (nameDef.getParent() instanceof LuaField) {
            LuaField field = (LuaField) nameDef.getParent();
            LuaExpr expr = PsiTreeUtil.findChildOfType(field, LuaExpr.class);
            if (expr != null) return expr.guessType();
        }
        //变量声明，local x = 0
        else {
            LuaLocalDef localDef = PsiTreeUtil.getParentOfType(nameDef, LuaLocalDef.class);
            if (localDef != null) {
                LuaComment comment = localDef.getComment();
                if (comment != null) {
                    LuaTypeSet typeSet = comment.guessType();
                    if (typeSet != null) return typeSet;
                }

                //计算 expr 返回类型
                LuaNameList nameList = localDef.getNameList();
                LuaExprList exprList = localDef.getExprList();
                if (nameList != null && exprList != null) {
                    int index = nameList.getNameDefList().indexOf(nameDef);
                    if (index != -1) {
                        LuaExpr expr = exprList.getExprList().get(index);
                        if (expr != null) {
                            return expr.guessType();
                        }
                    }
                }
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
}
