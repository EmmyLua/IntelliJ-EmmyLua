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

package com.tang.intellij.lua.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.CustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.tree.IReparseableElementType;
import com.intellij.util.CharTable;
import com.tang.intellij.lua.comment.lexer.LuaDocLexerAdapter;
import com.tang.intellij.lua.comment.parser.LuaDocParser;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.LuaParserDefinition;
import com.tang.intellij.lua.lexer.LuaLexerAdapter;
import com.tang.intellij.lua.parser.LuaParser;
import com.tang.intellij.lua.stubs.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaElementType extends IElementType {
    public LuaElementType(String debugName) {
        super(debugName, LuaLanguage.INSTANCE);
    }

    public static CustomParsingType DOC_COMMENT = new CustomParsingType ("DOC_COMMENT", LuaLanguage.INSTANCE) {
        @NotNull
        @Override
        public ASTNode parse(@NotNull CharSequence charSequence, @NotNull CharTable charTable) {
            PsiParser parser = new LuaDocParser();
            PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(
                    new LuaParserDefinition(),
                    new LuaDocLexerAdapter(),
                    charSequence);
            return parser.parse(this, builder);
        }
    };

    public static IStubElementType FUNC_DEF = new LuaFuncType();
    public static IStubElementType CLASS_METHOD_DEF = new LuaClassMethodType();
    public static LuaStubElementType CLASS_FIELD_DEF = new LuaDocClassFieldType();
    public static LuaStubElementType TYPE_DEF = new LuaDocTypeDefType();
    public static LuaStubElementType CLASS_DEF = new LuaDocClassType();
    public static LuaStubElementType DOC_TABLE_DEF = new LuaDocTableDefType();
    public static LuaStubElementType DOC_TABLE_FIELD_DEF = new LuaDocTableFieldDefType();
    public static IStubElementType TABLE = new LuaTableExprType();
    public static IStubElementType TABLE_FIELD = new LuaTableFieldType();
    public static IStubElementType INDEX = new LuaIndexExprType();
    public static IStubElementType NAME_EXPR = new LuaNameExprType();
    public static ILazyParseableElementType BLOCK = new LuaBlockElementType();

    static class LuaBlockElementType extends IReparseableElementType {

        LuaBlockElementType() {
            super("LuaBlock", LuaLanguage.INSTANCE);
        }

        @Override
        public ASTNode parseContents(@NotNull ASTNode chameleon) {
            Project project = chameleon.getPsi().getProject();
            PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(
                    project,
                    chameleon,
                    new LuaLexerAdapter(),
                    LuaLanguage.INSTANCE,
                    chameleon.getText());
            PsiParser luaParser = new LuaParser();
            return luaParser.parse(this, builder).getFirstChildNode();
        }

        @Nullable
        @Override
        public ASTNode createNode(CharSequence text) {
            return null;
        }
    }

    public static LuaNameDefElementType NAME_DEF = new LuaNameDefElementType();
    public static ParamNameDefElementType PARAM_NAME_DEF = new ParamNameDefElementType();
    public static LuaLiteralElementType LITERAL_EXPR = new LuaLiteralElementType();
}
