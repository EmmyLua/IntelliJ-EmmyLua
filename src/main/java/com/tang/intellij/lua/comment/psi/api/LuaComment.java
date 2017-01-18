package com.tang.intellij.lua.comment.psi.api;

import com.intellij.psi.PsiComment;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocParamDef;
import com.tang.intellij.lua.comment.psi.LuaDocPsiElement;
import com.tang.intellij.lua.comment.psi.LuaDocTypeDef;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.search.SearchContext;

/**
 * Created by Tangzx on 2016/11/21.
 *
 * @qq 272669294
 */
public interface LuaComment extends PsiComment, LuaDocPsiElement {
    LuaCommentOwner getOwner();
    LuaDocParamDef getParamDef(String name);
    LuaDocClassDef getClassDef();
    LuaDocTypeDef getTypeDef();
    LuaTypeSet guessType(SearchContext context);
}
