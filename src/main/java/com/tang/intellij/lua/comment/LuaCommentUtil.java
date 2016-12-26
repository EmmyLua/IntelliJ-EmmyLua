package com.tang.intellij.lua.comment;

import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.psi.LuaDocPsiElement;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
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
        if (comment.getParent() instanceof LuaCommentOwner)
            return (LuaCommentOwner) comment.getParent();
        return null;
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

    public static LuaComment findComment(LuaCommentOwner element) {
        return PsiTreeUtil.getChildOfType(element,  LuaComment.class);
    }
}
