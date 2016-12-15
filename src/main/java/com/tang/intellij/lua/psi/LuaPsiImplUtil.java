package com.tang.intellij.lua.psi;

import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocTypeDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import com.tang.intellij.lua.reference.LuaIndexReference;
import com.tang.intellij.lua.reference.LuaNameReference;
import com.tang.intellij.lua.reference.LuaRequireReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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

    /**
     * 寻找 Comment
     * @param declaration owner
     * @return LuaComment
     */
    public static LuaComment getComment(LuaDeclaration declaration) {
        return LuaCommentUtil.findComment(declaration);
    }

    /**
     * 取方法体名
     * @param classMethodDef def
     * @return String
     */
    public static String getMethodName(LuaClassMethodDef classMethodDef) {
        PsiElement postfixName = classMethodDef.getClassMethodName().getNameDef();
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
        LuaNameDef nameDef = globalFuncDef.getNameDef();
        return nameDef != null ? nameDef.getName() : null;
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

    public static LuaTypeSet guessType(LuaFuncCall funcCall) {
        LuaCallExpr callExpr = (LuaCallExpr)funcCall.getFirstChild();
        if (callExpr == null) return null;
        else return callExpr.guessType();
    }

    /**
     * 猜出前面的类型
     * @param callExpr call expr
     * @return LuaTypeSet
     */
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

    /**
     * 找出函数体
     * @param callExpr call expr
     * @return LuaFuncBodyOwner
     */
    public static LuaFuncBodyOwner resolveFuncBodyOwner(LuaCallExpr callExpr) {
        LuaArgs args = callExpr.getArgs();
        if (args != null) {
            PsiElement id = callExpr.getId();
            if (id == null) { // local, global, static
                LuaNameRef luaNameRef = PsiTreeUtil.getPrevSiblingOfType(args, LuaNameRef.class);
                if (luaNameRef != null)
                    return LuaPsiResolveUtil.resolveFuncBodyOwner(luaNameRef);
            } else {
                LuaTypeSet typeSet = callExpr.guessPrefixType();
                if (typeSet != null && !typeSet.isEmpty()) { //TODO multi-type
                    // class method
                    LuaType type = typeSet.getType(0);
                    return LuaClassMethodIndex.findMethodWithName(type.getClassNameText(), id.getText(), callExpr.getProject(), new ProjectAndLibrariesScope(callExpr.getProject()));
                }
            }
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
            } else if (def instanceof LuaNameRef) { // TODO : Global assign
                LuaAssignStat luaAssignStat = PsiTreeUtil.getParentOfType(def, LuaAssignStat.class);
                if (luaAssignStat != null) {
                    LuaComment comment = luaAssignStat.getComment();
                    if (comment != null) {
                        LuaDocClassDef classDef = PsiTreeUtil.findChildOfType(comment, LuaDocClassDef.class);
                        if (classDef != null) {
                            return LuaTypeSet.create(classDef);
                        }
                    }
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

    private static final String[] EMPTY_PARAMETERS = new String[0];
    public static String[] getParameters(LuaFuncBodyOwner funcBodyOwner) {
        LuaFuncBody body = funcBodyOwner.getFuncBody();
        if (body != null) {
            List<LuaParDef> parDefList = body.getParDefList();
            String[] array = new String[parDefList.size()];
            for (int i = 0; i < parDefList.size(); i++) {
                LuaParDef parDef = parDefList.get(i);
                array[i] = parDef.getName();
            }
            return array;
        }

        return EMPTY_PARAMETERS;
    }
}
