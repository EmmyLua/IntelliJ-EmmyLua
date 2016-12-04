package com.tang.intellij.lua.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.doc.LuaCommentUtil;
import com.tang.intellij.lua.doc.psi.LuaDocClassNameRef;
import com.tang.intellij.lua.doc.psi.LuaDocReturnDef;
import com.tang.intellij.lua.doc.psi.LuaDocTypeList;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.psi.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
                        LuaDocTypeList typeList = returnDef.getTypeList();
                        if (typeList != null) {
                            List<LuaDocClassNameRef> classNameRefList = typeList.getClassNameRefList();
                            if (classNameRefList.size() > 0) {
                                LuaDocClassNameRef ref = classNameRefList.get(0);
                                String name = ref.getText();
                                return LuaTypeSet.create(LuaClassIndex.find(name, getProject(), new ProjectAndLibrariesScope(getProject())));
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    static LuaTypeSet guessType(LuaValueExpr valueExpr) {
        PsiElement firstChild = valueExpr.getFirstChild();
        if (firstChild instanceof LuaFuncCall) {
            return ((LuaFuncCall) firstChild).guessType();
        }
        return null;
    }
}
