package com.tang.intellij.lua.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.lang.type.LuaTypeTable;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.psi.LuaField;
import com.tang.intellij.lua.psi.LuaFieldList;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/12/4.
 */
public class LuaIndexReference extends PsiReferenceBase<LuaIndexExpr> {

    PsiElement id;

    public LuaIndexReference(@NotNull LuaIndexExpr element, PsiElement id) {
        super(element);
        this.id = id;
    }


    @Override
    public TextRange getRangeInElement() {

        return new TextRange(id.getStartOffsetInParent(), id.getStartOffsetInParent() + id.getTextLength());
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement newId = LuaElementFactory.createIdentifier(myElement.getProject(), newElementName);
        id.replace(newId);
        return newId;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return true;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        LuaTypeSet typeSet = myElement.guessPrefixType();
        if (typeSet != null) {
            String idString = id.getText();
            for (LuaType type : typeSet.getTypes()) {
                if (type instanceof LuaTypeTable) { // 可能是 table 字段
                    LuaTypeTable tableType = (LuaTypeTable) type;
                    LuaFieldList fieldList = tableType.tableConstructor.getFieldList();
                    if (fieldList != null) {
                        for (LuaField field : fieldList.getFieldList()) {
                            PsiElement nameId = field.getNameDef();
                            if (nameId != null && idString.equals(nameId.getText())) {
                                return nameId;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
