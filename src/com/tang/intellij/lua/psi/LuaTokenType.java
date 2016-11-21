package com.tang.intellij.lua.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.doc.psi.impl.LuaCommentImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import org.jetbrains.annotations.Nullable;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaTokenType extends IElementType {
    public LuaTokenType(String debugName) {
        super(debugName, LuaLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "LuaTokenType." + super.toString();
    }

    public static ILazyParseableElementType DOC_COMMENT = new ILazyParseableElementType("DOC_COMMENT", LuaLanguage.INSTANCE) {

        @Override
        public ASTNode parseContents(ASTNode chameleon) {
            return super.parseContents(chameleon);
        }

        @Nullable
        @Override
        public ASTNode createNode(CharSequence text) {
            return new LuaCommentImpl(text);
        }
    };
}
