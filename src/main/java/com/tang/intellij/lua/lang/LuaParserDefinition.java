package com.tang.intellij.lua.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.tang.intellij.lua.comment.psi.LuaDocElementType;
import com.tang.intellij.lua.comment.psi.LuaDocTypes;
import com.tang.intellij.lua.lexer.LuaLexerAdapter;
import com.tang.intellij.lua.parser.LuaParser;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.LuaFile;
import com.tang.intellij.lua.psi.LuaTokenType;
import com.tang.intellij.lua.psi.LuaTypes;
import com.tang.intellij.lua.stubs.types.LuaFileStubElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaParserDefinition implements ParserDefinition {

    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(LuaTypes.SHORT_COMMENT, LuaTypes.BLOCK_COMMENT, LuaTypes.DOC_COMMENT);
    public static final TokenSet STRINGS =TokenSet.create(LuaTypes.STRING);

    public static final LuaFileStubElementType FILE = new LuaFileStubElementType();

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new LuaLexerAdapter();
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return STRINGS;
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new LuaParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new LuaFile(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        IElementType type = node.getElementType();
        if (type instanceof LuaDocElementType
                || type == LuaElementType.CLASS_DEF
                || type == LuaElementType.CLASS_FIELD_DEF
                || type == LuaElementType.GLOBAL_FIELD_DEF) {
            return LuaDocTypes.Factory.createElement(node);
        }
        return LuaTypes.Factory.createElement(node);
    }

    public static IElementType createType(String string) {
        if ("GLOBAL_FUNC_DEF".equals(string))
            return LuaElementType.GLOBAL_FUNC_DEF;
        else if ("CLASS_METHOD_DEF".equals(string))
            return LuaElementType.CLASS_METHOD_DEF;
        else if ("BLOCK".equals(string))
            return LuaElementType.BLOCK;
        else if ("TABLE_CONSTRUCTOR".equals(string))
            return LuaElementType.TABLE;
        else if ("TABLE_FIELD".equals(string))
            return LuaElementType.TABLE_FIELD;
        else if ("VAR".equals(string))
            return LuaElementType.VAR;

        return new LuaElementType(string);
    }

    public static IElementType createToken(String string) {
        if (string.equals("DOC_COMMENT"))
            return LuaElementType.DOC_COMMENT;

        return new LuaTokenType(string);
    }

    public static IElementType createDocType(String string) {
        if ("CLASS_DEF".equals(string))
            return LuaElementType.CLASS_DEF;
        if ("FIELD_DEF".equals(string))
            return LuaElementType.CLASS_FIELD_DEF;
        else if ("GLOBAL_DEF".equals(string))
            return LuaElementType.GLOBAL_FIELD_DEF;

        return new LuaDocElementType(string);
    }
}
