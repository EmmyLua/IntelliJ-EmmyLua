package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.doc.psi.LuaDocParamDef;
import com.tang.intellij.lua.doc.psi.LuaDocTypeDef;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.psi.index.LuaClassIndex;

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

    static LuaDocClassDef resolveType(LuaNameDef nameDef) {
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
        //变量声明，local x = 0
        else {
            LuaCommentOwner owner = PsiTreeUtil.getParentOfType(nameDef, LuaCommentOwner.class);
            if (owner != null) {
                LuaComment comment = owner.getComment();
                if (comment != null) {
                    LuaDocClassDef def = comment.getClassDef(); // @class XXX
                    if (def != null)
                        return def;
                    else { // @type xxx
                        LuaDocTypeDef typeDef = comment.getTypeDef();
                        if (typeDef != null && typeDef.getClassNameRef() != null) {
                            return LuaClassIndex.find(typeDef.getClassNameRef().getText(), nameDef.getProject(), new ProjectAndLibrariesScope(nameDef.getProject()));
                        }
                    }
                }
            }
        }
        return null;
    }
}
