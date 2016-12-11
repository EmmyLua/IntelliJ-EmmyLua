package com.tang.intellij.lua.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocTypeDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.reference.LuaIndexReference;
import com.tang.intellij.lua.reference.LuaNameReference;
import com.tang.intellij.lua.reference.LuaRequireReference;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public static String getName(LuaNamedElement identifier) {
        return identifier.getText();
    }

    public static LuaTypeSet resolveType(LuaNameDef nameDef) {
        return LuaPsiResolveUtil.resolveType(nameDef);
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

    public static LuaTypeSet resolveType(LuaParDef parDef) {
        return LuaPsiResolveUtil.resolveType(parDef);
    }

    public static LuaComment getComment(LuaDeclaration globalFuncDef) {
        return LuaCommentUtil.findComment(globalFuncDef);
    }

    public static String getName(LuaGlobalFuncDef globalFuncDef) {
        if (globalFuncDef.getStub() != null)
            return globalFuncDef.getStub().getName();
        LuaNameDef nameDef = globalFuncDef.getNameDef();
        return nameDef != null ? nameDef.getName() : null;
    }

    public static LuaTypeSet guessType(LuaFuncCall funcCall) {
        LuaCallExpr callExpr = (LuaCallExpr)funcCall.getFirstChild();
        if (callExpr == null) return null;
        else return callExpr.guessType();
    }

    public static LuaTypeSet guessPrefixType(LuaCallExpr callExpr) {
        LuaNameRef nameRef = callExpr.getNameRef();
        if (nameRef != null) {
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
                        LuaDocTypeDef typeDef = comment.getTypeDef();
                        if (typeDef != null)
                            return typeDef.guessType();
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

    public static PsiReference getReference(LuaCallExpr callExpr) {
        PsiElement id = callExpr.getNameRef();
        if (id != null && id.getText().equals("require")) {
            LuaArgs args = callExpr.getArgs();
            if (args != null) {
                PsiElement path = args.getFirstChild();
                if (path != null && path.getNode().getElementType() == LuaTypes.STRING) {
                    String pathString = path.getText();
                    pathString = pathString.substring(1, pathString.length() - 1);
                    int start = args.getStartOffsetInParent() + 1;
                    int end = start + path.getTextLength() - 2;
                    return new LuaRequireReference(callExpr, new TextRange(start, end), pathString);
                }
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
            }
        }
        else {
            LuaExpr prefix = (LuaExpr) indexExpr.getFirstChild();
            if (prefix != null)
                return prefix.guessType();
        }
        return null;
    }

    public static PsiReference getReference(LuaIndexExpr indexExpr) {
        PsiElement id = indexExpr.getId();
        if (id != null) return new LuaIndexReference(indexExpr, id);
        else return null;
    }

    public static LuaField findField(LuaTableConstructor table, String fieldName) {
        LuaFieldList fieldList = table.getFieldList();
        if (fieldList != null) {
            for (LuaField field : fieldList.getFieldList()) {
                LuaNameDef id = field.getNameDef();
                if (id != null && fieldName.equals(id.getName()))
                    return field;
            }
        }
        return null;
    }
}
