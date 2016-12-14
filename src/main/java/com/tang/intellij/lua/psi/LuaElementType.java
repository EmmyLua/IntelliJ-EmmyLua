package com.tang.intellij.lua.psi;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.tree.IReparseableElementType;
import com.tang.intellij.lua.comment.lexer.LuaDocLexerAdapter;
import com.tang.intellij.lua.comment.parser.LuaDocParser;
import com.tang.intellij.lua.comment.psi.impl.LuaCommentImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lexer.LuaLexerAdapter;
import com.tang.intellij.lua.parser.LuaParser;
import com.tang.intellij.lua.stubs.types.*;
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
    public static LuaClassMethodStubElementType CLASS_METHOD_DEF = new LuaClassMethodStubElementType();
    public static LuaClassFieldStubElementType CLASS_FIELD_DEF = new LuaClassFieldStubElementType();
    public static LuaGlobalFieldStubElementType GLOBAL_FIELD_DEF = new LuaGlobalFieldStubElementType();
    public static LuaClassStubElementType CLASS_DEF = new LuaClassStubElementType();
    public static ILazyParseableElementType BLOCK = new LuaBlockElementType();

    static class LuaBlockElementType extends IReparseableElementType {

        public LuaBlockElementType() {
            super("LuaBlock", LuaLanguage.INSTANCE);
        }

        @Override
        public ASTNode parseContents(ASTNode chameleon) {
            PsiElement psiFile = chameleon.getPsi();
            assert psiFile != null;

            Project project = psiFile.getProject();
            PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(
                    project,
                    chameleon,
                    new LuaLexerAdapter(),
                    LuaLanguage.INSTANCE,
                    chameleon.getText());
            LuaParser luaParser = new LuaParser();
            return luaParser.parse(LuaTypes.BLOCK, builder).getFirstChildNode();
        }

        @Nullable
        @Override
        public ASTNode createNode(CharSequence text) {
            return null;
        }
    }
}
