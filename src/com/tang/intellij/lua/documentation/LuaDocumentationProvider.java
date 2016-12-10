package com.tang.intellij.lua.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import org.jetbrains.annotations.Nullable;

/**
 * Documentation support
 * Created by tangzx on 2016/12/10.
 */
public class LuaDocumentationProvider extends AbstractDocumentationProvider implements DocumentationProvider {

    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        //TODO Documentation support
        if (element instanceof LuaCommentOwner) {
            LuaComment comment = LuaCommentUtil.findComment((LuaCommentOwner) element);
            if (comment != null) {
                return comment.getText();
            }
        }
        return super.generateDoc(element, originalElement);
    }
}
