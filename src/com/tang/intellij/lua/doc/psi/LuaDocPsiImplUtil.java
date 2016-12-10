package com.tang.intellij.lua.doc.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.doc.LuaCommentUtil;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.doc.reference.LuaClassNameReference;
import com.tang.intellij.lua.doc.reference.LuaDocParamNameReference;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaDocPsiImplUtil {

    @NotNull
    public static PsiReference getReference(LuaDocParamNameRef paramNameRef) {
        return new LuaDocParamNameReference(paramNameRef);
    }

    @NotNull
    public static PsiReference getReference(LuaDocClassNameRef docClassNameRef) {
        return new LuaClassNameReference(docClassNameRef);
    }

    @NotNull
    public static String getName(LuaDocClassName className) {
        return className.getText();
    }

    public static PsiElement setName(LuaDocClassName className, String newName) {
        return null;
    }

    public static LuaTypeSet resolveType(LuaDocGlobalDef docGlobalDef) {
        LuaComment comment = LuaCommentUtil.findContainer(docGlobalDef);
        LuaDocTypeDef docTypeDef = comment.getTypeDef();
        if (docTypeDef != null) {
            return resolveDocTypeSet(docTypeDef.getTypeSet(), null);
        }
        return null;
    }

    public static LuaTypeSet resolveType(LuaDocParamDef paramDec) {
        LuaDocTypeSet docTypeSet = paramDec.getTypeSet();
        if (docTypeSet == null) return null;
        return resolveDocTypeSet(docTypeSet, null);
    }

    public static LuaTypeSet resolveTypeAt(LuaDocReturnDef returnDef, int index) {
        LuaTypeSet typeSet = LuaTypeSet.create();
        LuaDocTypeList typeList = returnDef.getTypeList();
        if (typeList != null) {
            List<LuaDocTypeSet> typeSetList = typeList.getTypeSetList();
            LuaDocTypeSet docTypeSet = typeSetList.get(index);
            resolveDocTypeSet(docTypeSet, typeSet);
        }
        return typeSet;
    }

    static LuaTypeSet resolveDocTypeSet(LuaDocTypeSet docTypeSet, LuaTypeSet typeSet ) {
        if (typeSet == null) typeSet = LuaTypeSet.create();
        if (docTypeSet != null) {
            List<LuaDocClassNameRef> classNameRefList = docTypeSet.getClassNameRefList();
            for (LuaDocClassNameRef classNameRef : classNameRefList) {
                LuaDocClassDef def = LuaClassIndex.find(classNameRef.getText(), docTypeSet.getProject(), new ProjectAndLibrariesScope(docTypeSet.getProject()));
                if (def != null) {
                    typeSet.addType(def);
                }
            }
        }
        return typeSet;
    }

    public static String getClassNameText(LuaDocClassDef classDef) {
        return classDef.getClassName().getName();
    }

    public static LuaTypeSet guessType(LuaDocTypeDef typeDef) {
        return resolveDocTypeSet(typeDef.getTypeSet(), null);
    }
}
