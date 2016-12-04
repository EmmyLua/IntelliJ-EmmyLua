package com.tang.intellij.lua.doc.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.doc.reference.LuaClassNameReference;
import com.tang.intellij.lua.doc.reference.LuaDocParamNameReference;
import com.tang.intellij.lua.lang.type.LuaType;
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

    public static LuaTypeSet resolveType(LuaDocParamDef paramDec) {
        LuaDocClassNameRef ref = paramDec.getClassNameRef();
        if (ref == null) return null;

        LuaDocClassDef def = LuaClassIndex.find(ref.getText(), paramDec.getProject(), new ProjectAndLibrariesScope(paramDec.getProject()));
        if (def != null) {
            return LuaTypeSet.create(def);
        }
        return null;
    }

    public static LuaTypeSet resolveTypeAt(LuaDocReturnDef returnDef, int index) {
        LuaTypeSet typeSet = LuaTypeSet.create();
        LuaDocTypeList typeList = returnDef.getTypeList();
        if (typeList != null) {
            List<LuaDocTypeSet> typeSetList = typeList.getTypeSetList();
            LuaDocTypeSet docTypeSet = typeSetList.get(index);
            if (docTypeSet != null) {
                List<LuaDocClassNameRef> classNameRefList = docTypeSet.getClassNameRefList();
                for (LuaDocClassNameRef classNameRef : classNameRefList) {
                    LuaDocClassDef def = LuaClassIndex.find(classNameRef.getText(), returnDef.getProject(), new ProjectAndLibrariesScope(returnDef.getProject()));
                    if (def != null) {
                        typeSet.addType(def);
                    }
                }
            }
        }
        return typeSet;
    }

    public static String getClassNameText(LuaDocClassDef classDef) {
        return classDef.getClassName().getName();
    }
}
