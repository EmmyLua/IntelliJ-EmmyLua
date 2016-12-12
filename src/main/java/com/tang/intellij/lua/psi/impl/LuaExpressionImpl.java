package com.tang.intellij.lua.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.comment.psi.LuaDocReturnDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.lang.type.LuaTypeTable;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.psi.index.LuaClassFieldIndex;
import org.jetbrains.annotations.NotNull;

/**
 * 表达式基类
 * Created by TangZX on 2016/12/4.
 */
public class LuaExpressionImpl extends LuaPsiElementImpl implements LuaExpression {
    public LuaExpressionImpl(@NotNull ASTNode node) {
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
                    LuaDocFieldDef fieldDef = LuaClassFieldIndex.find(type.getClassNameText(), propName, project, scope);
                    if (fieldDef != null) {
                        return fieldDef.resolveType();
                    }
                }
            }
        }
        return null;
    }

    private LuaTypeSet guessType(LuaCallExpr luaCallExpr) {
        LuaNameRef nameRef = luaCallExpr.getNameRef();
        if (nameRef != null) {
            PsiElement funRef = nameRef.resolve();
            if (funRef != null && funRef.getParent() instanceof LuaCommentOwner) { // 获取 ---@return CLASS
                LuaComment comment = LuaCommentUtil.findComment((LuaCommentOwner) funRef.getParent());
                if (comment != null) {
                    LuaDocReturnDef returnDef = PsiTreeUtil.findChildOfType(comment, LuaDocReturnDef.class);
                    if (returnDef != null) {
                        return returnDef.resolveTypeAt(0);
                    }
                }
            }
        }
        return null;
    }

    static LuaTypeSet guessType(LuaValueExpr valueExpr) {
        PsiElement firstChild = valueExpr.getFirstChild();
        if (firstChild != null) {
            if (firstChild instanceof LuaFuncCall) {
                return ((LuaFuncCall) firstChild).guessType();
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
            } else {
                //IElementType type = firstChild.getNode().getElementType();
                //TODO STRING TYPESET
                //TODO bool typeset
            }
        }
        return null;
    }
}
