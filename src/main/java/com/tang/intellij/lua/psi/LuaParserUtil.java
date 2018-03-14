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

import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.lang.WhitespacesBinders;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.tang.intellij.lua.psi.parser.LuaExpressionParser;
import com.tang.intellij.lua.psi.parser.LuaStatementParser;

import static com.tang.intellij.lua.psi.LuaTypes.*;

@SuppressWarnings("unused")
public class LuaParserUtil extends GeneratedParserUtilBase {

    /**
     * right:
     * --- comment
     * local obj
     *
     * wrong:
     * --- comment
     *
     * local obj
     */
    public static WhitespacesAndCommentsBinder MY_LEFT_COMMENT_BINDER = (list, b, tokenTextGetter) -> {
        int lines = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            IElementType type = list.get(i);
            if (type == DOC_COMMENT) {
                return i;
            } else {
                String sequence = String.valueOf(tokenTextGetter.get(i));
                lines += StringUtil.getLineBreakCount(sequence);
                if (lines > 1) {
                    break;
                }
            }
        }
        return list.size();
    };

    public static WhitespacesAndCommentsBinder MY_RIGHT_COMMENT_BINDER = (list, b, tokenTextGetter) -> {
        for (int i = 0; i < list.size(); i++) {
            IElementType type = list.get(i);
            if (type == DOC_COMMENT) {
                return i + 1;
            } else {
                String sequence = String.valueOf(tokenTextGetter.get(i));
                if (sequence.contains("\n")) {
                    break;
                }
            }
        }
        return 0;
    };

    public static boolean repeat(PsiBuilder builder_, int level_, Parser parser, int times) {
        PsiBuilder.Marker marker = builder_.mark();
        boolean r = true;
        for (int i = 0; r && i < times; i++) {
            r = parser.parse(builder_, level_);
        }
        marker.rollbackTo();
        return r;
    }

    public static boolean checkType(PsiBuilder builder_, int level_, IElementType type) {
        LighterASTNode marker = builder_.getLatestDoneMarker();
        return marker != null && marker.getTokenType() == type;
    }

    public static boolean fastCheckArgs(PsiBuilder builder_, int level_) {
        boolean r;
        PsiBuilder.Marker marker = builder_.mark();
        r = builder_.getTokenType() == LPAREN;
        if (r) {
            int lTimes = 1;
            while (true) {
                builder_.advanceLexer();
                IElementType type = builder_.getTokenType();
                if (type == LPAREN)
                    lTimes++;
                else if (type == RPAREN)
                    lTimes--;
                if (type == null || lTimes == 0) {
                    break;
                }
            }
            r = lTimes == 0;
        }
        if (!r) marker.drop();
        return r;
    }

    public static boolean fastCheckTable(PsiBuilder builder_, int level_) {
        boolean r;
        PsiBuilder.Marker marker = builder_.mark();
        r = builder_.getTokenType() == LCURLY;
        if (r) {
            int lTimes = 1;
            while (true) {
                builder_.advanceLexer();
                IElementType type = builder_.getTokenType();
                if (type == LCURLY)
                    lTimes++;
                else if (type == RCURLY)
                    lTimes--;
                if (type == null || lTimes == 0) {
                    break;
                }
            }
            r = lTimes == 0;
        }
        if (!r) marker.drop();
        return r;
    }

    public static boolean lazyBlock(PsiBuilder builder_, int level_) {
        int i = 0;
        IElementType begin=  builder_.rawLookup(--i);
        while (begin == TokenType.WHITE_SPACE)
            begin = builder_.rawLookup(--i);

        if (begin != null) {
            PsiBuilder.Marker marker = builder_.mark();
            marker.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, null);
            if (begin == RPAREN)
                begin = FUNCTION;

            matchStart(true, builder_, 0, begin);
            marker.collapse(BLOCK);
            marker.setCustomEdgeTokenBinders(null, WhitespacesBinders.GREEDY_RIGHT_BINDER);
        }
        return true;
    }

    private static TokenSet END_SET = TokenSet.create(END);
    private static TokenSet IF_SKIPS = TokenSet.create(THEN, ELSE, ELSEIF);
    private static TokenSet REPEAT_TYPES = TokenSet.create(UNTIL);
    private static TokenSet THEN_TYPES1 = TokenSet.create(ELSE, ELSEIF, END);
    private static TokenSet THEN_SKIPS2 = TokenSet.create(ELSE, ELSEIF);
    private static TokenSet BRACE_L_SET = TokenSet.create(LCURLY, LBRACK, LPAREN);
    private static TokenSet BRACE_R_SET = TokenSet.create(RCURLY, RBRACK, RPAREN);

    private static boolean matchStart(boolean advanced, PsiBuilder builder, int level, IElementType begin) {
        if (begin == DO) {
            return matchEnd(advanced, builder, level, TokenSet.EMPTY, END_SET);
        }
        else if (begin == REPEAT) {
            return matchEnd(advanced, builder, level, TokenSet.EMPTY, REPEAT_TYPES);
        }
        else if (begin == IF) {
            return matchEnd(advanced, builder ,level, IF_SKIPS, END_SET);
        }
        else if (begin == THEN) {
            if (level == 0)
                return matchEnd(advanced, builder, level, TokenSet.EMPTY, THEN_TYPES1);
            else
                return matchEnd(advanced, builder, level, THEN_SKIPS2, END_SET);
        }
        else if (begin == ELSE) {
            return matchEnd(advanced, builder, level, TokenSet.EMPTY, END_SET);
        }
        else if (begin == FUNCTION) {
            return matchEnd(advanced, builder, level, TokenSet.EMPTY, END_SET);
        }
//        else if (BRACE_L_SET.contains(begin)) {
//            return matchBrace(advanced, builder, level, getRBrace(begin));
//        }
        return false;
    }

    private static IElementType getRBrace(IElementType type) {
        if (type == LCURLY) return RCURLY;
        if (type == LPAREN) return RPAREN;
        if (type == LBRACK) return RBRACK;
        return null;
    }

    private static boolean matchBrace(boolean advanced, PsiBuilder builder, int level, IElementType end) {
        if (!advanced)
            builder.advanceLexer();
        IElementType type = builder.getTokenType();
        while (true) {
            if (type == null || builder.eof()) {
                return false;
            }

            while (true) {
                if (type == end) {
                    if (level != 0)
                        builder.advanceLexer();
                    return true;
                }
                boolean matchBrace = false;
                if (BRACE_L_SET.contains(type)) {
                    matchBrace = matchBrace(false, builder, level + 1, getRBrace(type));
                }
                if (!matchBrace) break;
                type = builder.getTokenType();
            }

            builder.advanceLexer();
            type = builder.getTokenType();
        }
    }

    private static boolean matchEnd(boolean advanced, PsiBuilder builder, int level, TokenSet skips, TokenSet types) {
        if (!advanced)
            builder.advanceLexer();
        IElementType type = builder.getTokenType();

        while (true) {
            if (type == null || builder.eof()) {
                return false;
            }

            while (!skips.contains(type)) {
//                if (BRACE_R_SET.contains(type)) {
//                    return false;
//                }
                if (types.contains(type)) {
                    if (level != 0)
                        builder.advanceLexer();
                    return true;
                }
                boolean isMatched = matchStart(false, builder, level + 1, type);
                if (!isMatched)
                    break;
                type = builder.getTokenType();
            }

            builder.advanceLexer();
            type = builder.getTokenType();
        }
    }

    public static boolean parseExpr(PsiBuilder b, int l) {
        return LuaExpressionParser.INSTANCE.parseExpr(b, l) != null;
    }

    public static boolean parseStatement(PsiBuilder b, int l) {
        return LuaStatementParser.INSTANCE.parseStatement(b, l) != null;
    }
}
