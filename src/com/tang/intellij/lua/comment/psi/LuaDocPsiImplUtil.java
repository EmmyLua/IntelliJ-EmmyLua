package com.tang.intellij.lua.comment.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.comment.reference.LuaClassNameReference;
import com.tang.intellij.lua.comment.reference.LuaDocParamNameReference;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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

    public static LuaType resolveType(LuaDocClassNameRef nameRef) {
        Project project = nameRef.getProject();
        Collection<LuaDocClassDef> defs = LuaClassIndex.getInstance().get(nameRef.getText(), project, new ProjectAndLibrariesScope(project));
        if (!defs.isEmpty()) {
            LuaDocClassDef classDef = defs.iterator().next();
            return LuaType.create(classDef);
        }
        return null;
    }

    @NotNull
    public static String getName(LuaDocNamedElement className) {
        return className.getText();
    }

    public static PsiElement setName(LuaDocNamedElement className, String newName) {
        return null;
    }

    /**
     * 猜测全局定义的类型
     * @param docGlobalDef 全局定义
     * @return 类型集合
     */
    public static LuaTypeSet resolveType(LuaDocGlobalDef docGlobalDef) {
        LuaComment comment = LuaCommentUtil.findContainer(docGlobalDef);
        LuaDocTypeDef docTypeDef = comment.getTypeDef();
        if (docTypeDef != null) {
            return resolveDocTypeSet(docTypeDef.getTypeSet(), null);
        }
        return null;
    }

    /**
     * 猜测参数的类型
     * @param paramDec 参数定义
     * @return 类型集合
     */
    public static LuaTypeSet resolveType(LuaDocParamDef paramDec) {
        LuaDocTypeSet docTypeSet = paramDec.getTypeSet();
        if (docTypeSet == null) return null;
        return resolveDocTypeSet(docTypeSet, null);
    }

    /**
     * 获取返回类型
     * @param returnDef 返回定义
     * @param index 索引
     * @return 类型集合
     */
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

    /**
     * 获取Class名称
     * @param classDef class定义
     * @return 类名
     */
    public static String getClassNameText(LuaDocClassDef classDef) {
        if (classDef.getStub() != null) {
            return classDef.getStub().getClassName();
        }
        return classDef.getClassName().getName();
    }

    /**
     * 获取所有超类
     * @param classDef class 定义
     * @return 超类集合
     */
    public static LuaTypeSet getSuperClasses(LuaDocClassDef classDef) {
        LuaType type = LuaType.create(classDef);
        LuaType supper = type.getSuperClass();
        LuaTypeSet set = LuaTypeSet.create();
        while (supper != null) {
            set.addType(supper);
            supper = supper.getSuperClass();
        }
        return set;
    }

    /**
     * 获取父类
     * @param classDef class def
     * @return LuaType
     */
    public static LuaType getSuperClass(LuaDocClassDef classDef) {
        LuaDocClassNameRef supperRef = classDef.getClassNameRef();
        return supperRef != null ? supperRef.resolveType() : null;
    }

    /**
     * 猜测类型
     * @param typeDef 类型定义
     * @return 类型集合
     */
    public static LuaTypeSet guessType(LuaDocTypeDef typeDef) {
        return resolveDocTypeSet(typeDef.getTypeSet(), null);
    }
}
