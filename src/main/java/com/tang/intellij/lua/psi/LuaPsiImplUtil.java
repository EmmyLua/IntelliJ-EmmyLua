package com.tang.intellij.lua.psi;

import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.reference.LuaNameReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
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
        }
        return owner;
    }

    @NotNull
    public static String getName(LuaNamedElement identifier) {
        return identifier.getText();
    }

    public static LuaTypeSet resolveType(LuaNameDef nameDef) {
        return LuaPsiResolveUtil.resolveType(nameDef);
    }

    public static PsiElement getNameIdentifier(LuaNameDef nameDef) {
        return nameDef.getFirstChild();
    }

    @NotNull
    public static PsiReference getReference(LuaNameRef ref) {
        return new LuaNameReference(ref);
    }

    public static PsiElement resolve(LuaNameRef ref) {
        return LuaPsiResolveUtil.resolve(ref);
    }

    public static LuaTypeSet resolveType(LuaNameRef nameRef) {
        PsiElement target = nameRef.resolve();
        if (target instanceof LuaTypeResolvable) {
            LuaTypeResolvable typeResolvable = (LuaTypeResolvable) target;
            return typeResolvable.resolveType();
        }
        return null;
    }

    /**
     * 寻找 Comment
     * @param declaration owner
     * @return LuaComment
     */
    public static LuaComment getComment(LuaCommentOwner declaration) {
        return LuaCommentUtil.findComment(declaration);
    }

    /**
     * 取方法体名
     * @param classMethodDef def
     * @return String
     */
    public static String getMethodName(LuaClassMethodDef classMethodDef) {
        PsiElement postfixName = classMethodDef.getClassMethodName().getClassFuncNameDef();
        return postfixName.getText();
    }

    /**
     * 寻找 class method 对应的类名
     * @param classMethodDef def
     * @return 类名
     */
    public static String getClassName(LuaClassMethodDef classMethodDef) {
        if (classMethodDef.getStub() != null) {
            return classMethodDef.getStub().getClassName();
        }

        LuaNameRef ref = classMethodDef.getClassMethodName().getNameRef();
        String clazzName = null;
        if (ref != null) {
            LuaTypeSet typeSet = ref.resolveType();
            if (typeSet != null && !typeSet.isEmpty()) {
                clazzName = typeSet.getType(0).getClassNameText();
            }
        }
        return clazzName;
    }

    /**
     * 寻找对应的类
     * @param classMethodDef def
     * @return LuaType
     */
    public static LuaType getClassType(LuaClassMethodDef classMethodDef) {
        LuaNameRef ref = classMethodDef.getClassMethodName().getNameRef();
        if (ref != null) {
            LuaTypeSet typeSet = ref.resolveType();
            if (typeSet != null && !typeSet.isEmpty()) {
                return typeSet.getType(0);
            }
        }
        return null;
    }

    public static String getName(LuaGlobalFuncDef globalFuncDef) {
        if (globalFuncDef.getStub() != null)
            return globalFuncDef.getStub().getName();
        PsiElement id = getNameIdentifier(globalFuncDef);
        return id != null ? id.getText() : null;
    }

    public static PsiElement getNameIdentifier(LuaGlobalFuncDef globalFuncDef) {
        return globalFuncDef.getId();
    }

    public static int getTextOffset(LuaGlobalFuncDef globalFuncDef) {
        PsiElement id = getNameIdentifier(globalFuncDef);
        return id != null ? id.getTextOffset() : globalFuncDef.getTextOffset();
    }

    public static ItemPresentation getPresentation(LuaGlobalFuncDef globalFuncDef) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return globalFuncDef.getName();
            }

            @Nullable
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
    public static LuaTypeSet guessPrefixType(LuaCallExpr callExpr) {
        LuaNameRef nameRef = callExpr.getNameRef();
        if (nameRef != null) { // 形如 xx:method, xx.method
            PsiElement def = nameRef.resolve();
            if (def instanceof LuaTypeResolvable) {
                return ((LuaTypeResolvable) def).resolveType();
            } else if (def instanceof LuaNameRef) {
                // global
                // --- @define
                // --- @type XXX
                // myGlobal = ...
                LuaAssignStat assignStat = PsiTreeUtil.getParentOfType(def, LuaAssignStat.class);
                if (assignStat != null) {
                    LuaComment comment = LuaCommentUtil.findComment(assignStat);
                    if (comment != null) {
                        return comment.guessType();
                    }
                }
            }
        }
        else {
            LuaExpr prefix = (LuaExpr) callExpr.getFirstChild();
            if (prefix != null)
                return prefix.guessType();
        }
        return null;
    }

    /**
     * 找出函数体
     * @param callExpr call expr
     * @return LuaFuncBodyOwner
     */
    public static LuaFuncBodyOwner resolveFuncBodyOwner(LuaCallExpr callExpr) {
        LuaArgs args = callExpr.getArgs();
        if (args != null) {
            PsiElement id = callExpr.getId(); //todo static : xxx.method
            if (id == null) { // local, global, static
                LuaNameRef luaNameRef = PsiTreeUtil.getPrevSiblingOfType(args, LuaNameRef.class);
                if (luaNameRef != null)
                    return LuaPsiResolveUtil.resolveFuncBodyOwner(luaNameRef);
            } else {
                LuaTypeSet typeSet = callExpr.guessPrefixType();
                if (typeSet != null && !typeSet.isEmpty()) { //TODO multi-type
                    // class method
                    LuaType type = typeSet.getType(0);
                    return type.findMethod(id.getText(), callExpr.getProject(), new ProjectAndLibrariesScope(callExpr.getProject()));
                }
            }
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
        if (args != null) {
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
        return null;
    }

    public static LuaTypeSet guessTypeAt(LuaExprList list, int index) {
        int cur = 0;
        for (PsiElement child = list.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof LuaExpr) {
                if (cur == index) {
                    return ((LuaExpr)child).guessType();
                }
                cur++;
            }
        }
        return null;
    }

    public static LuaTypeSet guessPrefixType(LuaIndexExpr indexExpr) {
        LuaNameRef nameRef = indexExpr.getNameRef();
        if (nameRef != null) {
            PsiElement def = nameRef.resolve();
            if (def instanceof LuaTypeResolvable) {
                return ((LuaTypeResolvable) def).resolveType();
            } else if (def instanceof LuaNameRef) { // TODO : Global assign
                LuaAssignStat luaAssignStat = PsiTreeUtil.getParentOfType(def, LuaAssignStat.class);
                if (luaAssignStat != null) {
                    LuaComment comment = luaAssignStat.getComment();
                    //优先从 Comment 猜
                    if (comment != null) {
                        LuaTypeSet typeSet = comment.guessType();
                        if (typeSet != null)
                            return typeSet;
                    }
                    //再从赋值猜
                    LuaExprList exprList = luaAssignStat.getExprList();
                    if (exprList != null)
                        return exprList.guessTypeAt(0);//TODO : multi
                }
            }
        }
        else {
            LuaExpr prefix = (LuaExpr) indexExpr.getFirstChild();
            if (prefix != null)
                return prefix.guessType();
        }
        return null;
    }

    public static LuaTableField findField(LuaTableConstructor table, String fieldName) {
        LuaFieldList fieldList = table.getFieldList();
        if (fieldList != null) {
            for (LuaTableField field : fieldList.getTableFieldList()) {
                LuaNameDef id = field.getNameDef();
                if (id != null && fieldName.equals(id.getName()))
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

    static String getParamFingerprint(LuaFuncBodyOwner funcBodyOwner) {
        List<LuaParamNameDef> nameDefList = getParamNameDefList(funcBodyOwner);
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        if (nameDefList != null) {
            for (int i = 0; i < nameDefList.size(); i++) {
                LuaParamNameDef nameDef = nameDefList.get(i);
                if (i != 0)
                    builder.append(", ");
                builder.append(nameDef.getName());
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public static PsiElement getNameIdentifier(LuaLocalFuncDef localFuncDef) {
        return localFuncDef.getId();
    }

    public static String getName(LuaLocalFuncDef localFuncDef) {
        PsiElement id = getNameIdentifier(localFuncDef);
        return id != null ? id.getText() : null;
    }

    public static int getTextOffset(LuaLocalFuncDef localFuncDef) {
        PsiElement id = getNameIdentifier(localFuncDef);
        if (id != null) return id.getTextOffset();
        return localFuncDef.getTextOffset();
    }
}
