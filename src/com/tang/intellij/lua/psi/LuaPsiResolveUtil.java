package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.doc.psi.LuaDocParamDef;
import com.tang.intellij.lua.doc.psi.LuaDocTypeDef;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaTypeSet;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaPsiResolveUtil {

    static LuaNameDef resolveResult;

    public static PsiElement resolve(LuaNameRef ref) {
        String refName = ref.getText();

        //local 变量, 参数
        LuaPsiTreeUtil.walkUpLocalNameDef(ref, nameDef -> {
            if (refName.equals(nameDef.getName())) {
                resolveResult = nameDef;
                return false;
            }
            return true;
        });

        if (resolveResult == null) {
            //local 函数名
            LuaPsiTreeUtil.walkUpLocalFuncDef(ref, nameDef -> {
                if (refName.equals(nameDef.getName())) {
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

    static LuaTypeSet resolveType(LuaNameDef nameDef) {
        //作为函数参数，类型在函数注释里找
        if (nameDef instanceof LuaParDef) {
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
                    LuaDocClassDef def = comment.getClassDef(); // @class XXX
                    if (def != null)
                        return LuaTypeSet.create(def);
                    else { // @type xxx
                        LuaDocTypeDef typeDef = comment.getTypeDef();
                        if (typeDef != null) {
                            return typeDef.guessType();
                        }
                    }
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
}
