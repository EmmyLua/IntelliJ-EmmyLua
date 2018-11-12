/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.tang.intellij.lua.comment.psi.LuaDocElementType
import com.tang.intellij.lua.comment.psi.LuaDocTypes
import com.tang.intellij.lua.comment.psi.impl.LuaCommentImpl
import com.tang.intellij.lua.lexer.LuaLexerAdapter
import com.tang.intellij.lua.parser.LuaParser
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.stubs.LuaFileElementType

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
class LuaParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer {
        return LuaLexerAdapter()
    }

    override fun getWhitespaceTokens(): TokenSet {
        return WHITE_SPACES
    }

    override fun getCommentTokens(): TokenSet {
        return COMMENTS
    }

    override fun getStringLiteralElements(): TokenSet {
        return STRINGS
    }

    override fun createParser(project: Project): PsiParser {
        return LuaParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return LuaPsiFile(viewProvider)
    }

    override fun spaceExistanceTypeBetweenTokens(left: ASTNode, right: ASTNode): ParserDefinition.SpaceRequirements {
        return ParserDefinition.SpaceRequirements.MAY
    }

    override fun createElement(node: ASTNode): PsiElement {
        val type = node.elementType
        if (type === LuaElementType.DOC_COMMENT)
            return LuaCommentImpl(node)
        return if (type is LuaDocElementType
                || type === LuaElementType.DOC_TABLE_DEF
                || type === LuaElementType.DOC_TABLE_FIELD_DEF
                || type === LuaElementType.CLASS_DEF
                || type === LuaElementType.CLASS_FIELD_DEF
                || type === LuaElementType.TYPE_DEF) {
            LuaDocTypes.Factory.createElement(node)
        } else LuaTypes.Factory.createElement(node)
    }

    companion object {
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(
                LuaTypes.SHORT_COMMENT,
                LuaTypes.BLOCK_COMMENT,
                LuaTypes.DOC_COMMENT,
                LuaTypes.REGION,
                LuaTypes.ENDREGION
        )
        val STRINGS = TokenSet.create(LuaTypes.STRING)
        val KEYWORD_TOKENS = TokenSet.create(
                LuaTypes.AND,
                LuaTypes.BREAK,
                LuaTypes.DO,
                LuaTypes.ELSE,
                LuaTypes.ELSEIF,
                LuaTypes.END,
                LuaTypes.FOR,
                LuaTypes.FUNCTION,
                LuaTypes.IF,
                LuaTypes.IN,
                LuaTypes.LOCAL,
                LuaTypes.NOT,
                LuaTypes.OR,
                LuaTypes.REPEAT,
                LuaTypes.RETURN,
                LuaTypes.THEN,
                LuaTypes.UNTIL,
                LuaTypes.WHILE,

                //lua5.3
                LuaTypes.DOUBLE_COLON,
                LuaTypes.GOTO
        )
        val LUA52_BIN_OP_SET = TokenSet.create(
                LuaTypes.BIT_AND,
                LuaTypes.BIT_LTLT,
                LuaTypes.BIT_OR,
                LuaTypes.BIT_RTRT,
                LuaTypes.BIT_TILDE,
                LuaTypes.DOUBLE_DIV
        )
        val LUA52_UNARY_OP_SET = TokenSet.create(
                LuaTypes.BIT_TILDE
        )
        val PRIMITIVE_TYPE_SET = TokenSet.create(
                LuaTypes.FALSE,
                LuaTypes.NIL,
                LuaTypes.TRUE
        )
        val DOC_TAG_TOKENS = TokenSet.create(
                LuaDocTypes.TAG_NAME_PARAM,
                LuaDocTypes.TAG_NAME_RETURN,
                LuaDocTypes.TAG_NAME_CLASS,
                LuaDocTypes.TAG_NAME_MODULE,
                LuaDocTypes.TAG_NAME_TYPE,
                LuaDocTypes.TAG_NAME_FIELD,
                LuaDocTypes.TAG_NAME_LANGUAGE,
                LuaDocTypes.TAG_NAME_OVERLOAD,
                LuaDocTypes.TAG_NAME_PRIVATE,
                LuaDocTypes.TAG_NAME_PROTECTED,
                LuaDocTypes.TAG_NAME_PUBLIC,
                LuaDocTypes.TAG_NAME_SEE,
                LuaDocTypes.TAG_NAME_GENERIC,
                LuaDocTypes.TAG_NAME_VARARG
        )
        val DOC_KEYWORD_TOKENS = TokenSet.create(
                LuaDocTypes.FUN,
                LuaDocTypes.VARARG
        )
        val FILE = LuaFileElementType()
    }
}

fun createType(string: String): IElementType {
    return when (string) {
        "FUNC_DEF" -> LuaElementType.FUNC_DEF
        "CLASS_METHOD_DEF" -> LuaElementType.CLASS_METHOD_DEF
        "BLOCK" -> LuaElementType.BLOCK
        "TABLE_EXPR" -> LuaElementType.TABLE
        "TABLE_FIELD" -> LuaElementType.TABLE_FIELD
        "INDEX_EXPR" -> LuaElementType.INDEX
        "NAME_EXPR" -> LuaElementType.NAME_EXPR
        "NAME_DEF" -> LuaElementType.NAME_DEF
        "PARAM_NAME_DEF" -> LuaElementType.PARAM_NAME_DEF
        "LITERAL_EXPR" -> LuaElementType.LITERAL_EXPR

        "CALL_EXPR" -> LuaElementTypes.CALL_EXPR
        "SINGLE_ARG" -> LuaElementTypes.SINGLE_ARG
        "LIST_ARGS" -> LuaElementTypes.LIST_ARGS

        "EXPR_LIST" -> LuaElementTypes.EXPR_LIST
        "NAME_LIST" -> LuaElementTypes.NAME_LIST
        "LOCAL_DEF" -> LuaElementTypes.LOCAL_DEF
        "ASSIGN_STAT" -> LuaElementTypes.ASSIGN_STAT
        "VAR_LIST" -> LuaElementTypes.VAR_LIST
        "PAREN_EXPR" -> LuaElementTypes.PAREN_EXPR
        "LOCAL_FUNC_DEF" -> LuaElementTypes.LOCAL_FUNC_DEF
        "CLOSURE_EXPR" -> LuaElementTypes.CLOSURE_EXPR
        "FUNC_BODY" -> LuaElementTypes.FUNC_BODY
        "CLASS_METHOD_NAME" -> LuaElementTypes.CLASS_METHOD_NAME

        "RETURN_STAT" -> LuaElementTypes.RETURN_STAT
        "DO_STAT" -> LuaElementTypes.DO_STAT
        "IF_STAT" -> LuaElementTypes.IF_STAT
        "EXPR_STAT" -> LuaElementTypes.EXPR_STAT

        "UNARY_EXPR" -> LuaElementTypes.UNARY_EXPR
        "BINARY_EXPR" -> LuaElementTypes.BINARY_EXPR

        else -> LuaElementType(string)
    }
}

fun createToken(string: String): IElementType {
    return if (string == "DOC_COMMENT") LuaElementType.DOC_COMMENT else LuaTokenType(string)
}

fun createDocType(string: String): IElementType {
    return when (string) {
        "TAG_CLASS" -> LuaElementType.CLASS_DEF
        "TAG_FIELD" -> LuaElementType.CLASS_FIELD_DEF
        "TABLE_DEF" -> LuaElementType.DOC_TABLE_DEF
        "TABLE_FIELD" -> LuaElementType.DOC_TABLE_FIELD_DEF
        else -> if ("TAG_TYPE" == string) LuaElementType.TYPE_DEF else LuaDocElementType(string)
    }

}