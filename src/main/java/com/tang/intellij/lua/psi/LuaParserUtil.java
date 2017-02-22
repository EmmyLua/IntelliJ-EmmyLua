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
import com.intellij.psi.impl.source.tree.ElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.tang.intellij.lua.psi.LuaTypes.*;

public class LuaParserUtil extends GeneratedParserUtilBase {

    public static WhitespacesAndCommentsBinder MY_LEFT_COMMENT_BINDER = (list, b, tokenTextGetter) -> {
        for (int i = list.size() - 1; i >= 0; i--) {
            IElementType type = list.get(i);
            if (type == DOC_COMMENT) {
                return i;
            }
        }
        return list.size();
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
        while (begin == ElementType.WHITE_SPACE)
            begin = builder_.rawLookup(--i);

        if (begin != null) {
            PsiBuilder.Marker marker = builder_.mark();
            marker.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, null);
            if (begin == RPAREN)
                begin = FUNCTION;

            matchStart(builder_, 0, begin, true);
            marker.collapse(BLOCK);
            marker.setCustomEdgeTokenBinders(null, WhitespacesBinders.GREEDY_RIGHT_BINDER);
        }
        return true;
    }

    private static boolean matchStart(PsiBuilder builder, int level, IElementType begin, boolean advanced) {
        if (begin == DO) {
            return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(END));
        }
        else if (begin == REPEAT) {
            return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(UNTIL));
        }
        //--
        else if (begin == IF) {
            TokenSet skips = TokenSet.create(THEN, ELSE, ELSEIF);
            return matchEnd(begin, advanced, builder ,level, skips, TokenSet.create(END));
        }
        //--
        /*else if (begin == FOR) {
            TokenSet skips = TokenSet.create(DO);
            return matchEnd(begin, advanced, builder ,level, skips, TokenSet.create(END));
        }*/
        else if (begin == THEN) {
            if (level == 0)
                return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(ELSE, ELSEIF, END));
            else
                return matchEnd(begin, advanced, builder, level, TokenSet.create(ELSE, ELSEIF), TokenSet.create(END));
        }
        else if (begin == ELSE) {
            return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(END));
        }
        else if (begin == FUNCTION) {
            return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(END));
        }
        return false;
    }

    private static boolean matchEnd(IElementType start, boolean advanced, PsiBuilder builder, int level, TokenSet skips, TokenSet types) {
        if (!advanced)
            builder.advanceLexer();
        IElementType type = builder.getTokenType();

        while (true) {
            if (type == null || builder.eof()) {
                return false;
            }

            while (!skips.contains(type)) {
                if (types.contains(type)) {
                    if (level != 0)
                        builder.advanceLexer();
                    return true;
                }
                boolean isMatched = matchStart(builder, level + 1, type, false);
                if (!isMatched)
                    break;
                type = builder.getTokenType();
            }

            builder.advanceLexer();
            type = builder.getTokenType();
        }
    }
}
