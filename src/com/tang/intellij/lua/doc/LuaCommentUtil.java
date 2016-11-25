package com.tang.intellij.lua.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.doc.psi.LuaDocPsiElement;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.lang.LuaParserDefinition;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaCommentUtil {

    @Nullable
    public static LuaCommentOwner findOwner(LuaDocPsiElement element) {
        LuaComment comment = findContainer(element);
        PsiElement next = comment.getNextSibling();
        while (true) {
            if (next == null) return null;
            if (next instanceof LuaCommentOwner)
                return (LuaCommentOwner) next;
            IElementType type = next.getNode().getElementType();
            if (LuaParserDefinition.WHITE_SPACES.contains(type)) {
                next = next.getNextSibling();
            } else {
                return null;
            }
        }
    }

    @NotNull
    public static LuaComment findContainer(@NotNull LuaDocPsiElement element) {
        while (true) {
            if (element instanceof LuaComment) {
                return (LuaComment) element;
            }
            element = (LuaDocPsiElement) element.getParent();
        }
    }

    public static LuaComment findComment(PsiElement element) {
        return null;
    }

}
