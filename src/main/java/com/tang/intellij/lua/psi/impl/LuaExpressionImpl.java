package com.tang.intellij.lua.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocReturnDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.lang.type.LuaTypeTable;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * 表达式基类
 * Created by TangZX on 2016/12/4.
 */
public class LuaExpressionImpl extends LuaPsiElementImpl implements LuaExpression {
    LuaExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public LuaTypeSet guessType() {
        if (this instanceof LuaValueExpr)
            return guessType((LuaValueExpr) this);
        if (this instanceof LuaCallExpr)
            return guessType((LuaCallExpr) this);
        if (this instanceof LuaIndexExpr)
            return guessType((LuaIndexExpr) this);

        return null;
    }

    private LuaTypeSet guessType(LuaIndexExpr indexExpr) {
        PsiElement id = indexExpr.getId();
        if (id == null) return null;

        LuaTypeSet prefixType = indexExpr.guessPrefixType();
        if (prefixType != null && !prefixType.isEmpty()) {
            String propName = id.getText();
            for (LuaType type : prefixType.getTypes()) {
                if (type instanceof LuaTypeTable) {
                    LuaTypeTable table = (LuaTypeTable) type;
                    LuaField field = table.tableConstructor.findField(propName);
                    if (field != null) {
                        LuaExpr expr = PsiTreeUtil.findChildOfType(field, LuaExpr.class);
                        if (expr != null) return expr.guessType();
                    }
                } else {
                    Project project = indexExpr.getProject();
                    GlobalSearchScope scope = new ProjectAndLibrariesScope(project);
                    LuaTypeSet typeSet = type.guessFieldType(propName, project, scope);
                    if (typeSet != null)
                        return typeSet;
                }
            }
        }
        return null;
    }

    private LuaTypeSet guessType(LuaCallExpr luaCallExpr) {
        // xxx()
        LuaNameRef ref = luaCallExpr.getNameRef();
        if (ref != null) {
            // 从 require 'xxx' 中获取返回类型
            if (ref.textMatches("require")) {
                String filePath = null;
                PsiElement string = luaCallExpr.getFirstStringArg();
                if (string != null) {
                    filePath = string.getText();
                    filePath = filePath.substring(1, filePath.length() - 1);
                }
                LuaFile file = null;
                if (filePath != null)
                    file = LuaPsiResolveUtil.resolveRequireFile(filePath, luaCallExpr.getProject());
                if (file != null)
                    return file.getReturnedType();
            }
        }
        // find in comment
        LuaFuncBodyOwner bodyOwner = luaCallExpr.resolveFuncBodyOwner();
        if (bodyOwner instanceof LuaCommentOwner) {
            LuaComment comment = LuaCommentUtil.findComment((LuaCommentOwner) bodyOwner);
            if (comment != null) {
                LuaDocReturnDef returnDef = PsiTreeUtil.findChildOfType(comment, LuaDocReturnDef.class);
                if (returnDef != null) {
                    return returnDef.resolveTypeAt(0); //TODO : multi
                }
            }
        }
        return null;
    }

    private static LuaTypeSet guessType(LuaValueExpr valueExpr) {
        PsiElement firstChild = valueExpr.getFirstChild();
        if (firstChild != null) {
            if (firstChild instanceof LuaExpr) {
                return ((LuaExpr) firstChild).guessType();
            }
            else if (firstChild instanceof LuaTableConstructor) {
                return LuaTypeSet.create(LuaTypeTable.create((LuaTableConstructor) firstChild));
            }
            else if (firstChild instanceof LuaVar) {
                LuaVar luaVar = (LuaVar) firstChild;
                LuaNameRef ref = luaVar.getNameRef();
                if (ref != null)
                    return ref.resolveType();
                else if (luaVar.getExpr() != null)
                    return luaVar.getExpr().guessType();
            }
        }
        return null;
    }
}
