package com.tang.intellij.lua.psi;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.tang.intellij.lua.doc.lexer.LuaDocLexerAdapter;
import com.tang.intellij.lua.doc.parser.LuaDocParser;
import com.tang.intellij.lua.doc.psi.impl.LuaCommentImpl;
import com.tang.intellij.lua.psi.stub.elements.LuaGlobalFuncDefStubElementType;
import com.tang.intellij.lua.lang.LuaLanguage;
import org.jetbrains.annotations.Nullable;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaElementType extends IElementType {
    public LuaElementType(String debugName) {
        super(debugName, LuaLanguage.INSTANCE);
    }

    public static ILazyParseableElementType DOC_COMMENT = new ILazyParseableElementType("DOC_COMMENT", LuaLanguage.INSTANCE) {

        @Override
        public ASTNode parseContents(ASTNode chameleon) {
            PsiElement parentElement = chameleon.getTreeParent().getPsi();
            Project project = parentElement.getProject();
            Language languageForParser = this.getLanguageForParser(parentElement);
            PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(
                    project,
                    chameleon,
                    new LuaDocLexerAdapter(),
                    languageForParser,
                    chameleon.getChars());
            PsiParser parser = new LuaDocParser();
            ASTNode node = parser.parse(this, builder);
            return node.getFirstChildNode();
        }

        @Nullable
        @Override
        public ASTNode createNode(CharSequence text) {
            return new LuaCommentImpl(text);
        }
    };

    public static LuaGlobalFuncDefStubElementType GLOBAL_FUNC_DEF = new LuaGlobalFuncDefStubElementType();
}
