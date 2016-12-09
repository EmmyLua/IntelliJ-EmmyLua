package com.tang.intellij.lua.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.impl.source.tree.ElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ArrayUtil;

public class LuaParserUtil extends GeneratedParserUtilBase {

    public static boolean twoExpr(PsiBuilder builder_, int level_, Parser parser) {
        System.out.println("twoExpr");
        PsiBuilder.Marker marker = builder_.mark();
        boolean r = parser.parse(builder_, level_);
        r = r && parser.parse(builder_, level_);
        marker.rollbackTo();
        return r;
    }

    public static boolean checkNext(PsiBuilder builder_, int level_, Parser parser) {
        PsiBuilder.Marker marker = builder_.mark();
        boolean r = parser.parse(builder_, level_);
        marker.rollbackTo();
        return r;
    }

    public static boolean lazyBlock(PsiBuilder builder_, int level_) {
        int i = 0;
        IElementType begin=  builder_.rawLookup(--i);
        while (begin == ElementType.WHITE_SPACE)
            begin = builder_.rawLookup(--i);

        if (begin != null) {
            PsiBuilder.Marker marker = builder_.mark();
            if (begin == LuaTypes.RPAREN)
                begin = LuaTypes.FUNCTION;

            boolean r = matchStart(builder_, 0, begin, true);
            marker.collapse(LuaTypes.BLOCK);
        }
        return true;
    }

    static boolean matchStart(PsiBuilder builder, int level, IElementType begin, boolean advanced) {
        if (begin == LuaTypes.DO) {
            return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(LuaTypes.END));
        }
        else if (begin == LuaTypes.REPEAT) {
            return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(LuaTypes.UNTIL));
        }
        //--
        else if (begin == LuaTypes.IF) {
            TokenSet skips = TokenSet.create(LuaTypes.THEN, LuaTypes.ELSE, LuaTypes.ELSEIF);
            return matchEnd(begin, advanced, builder ,level, skips, TokenSet.create(LuaTypes.END));
        }
        //--
        else if (begin == LuaTypes.FOR) {
            TokenSet skips = TokenSet.create(LuaTypes.DO);
            return matchEnd(begin, advanced, builder ,level, skips, TokenSet.create(LuaTypes.END));
        }
        else if (begin == LuaTypes.THEN) {
            if (level == 0)
                return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(LuaTypes.ELSE, LuaTypes.ELSEIF, LuaTypes.END));
            else
                return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(LuaTypes.END));
        }
        else if (begin == LuaTypes.ELSE) {
            return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(LuaTypes.END));
        }
        else if (begin == LuaTypes.FUNCTION) {
            return matchEnd(begin, advanced, builder, level, TokenSet.EMPTY, TokenSet.create(LuaTypes.END));
        }
        return false;
    }

    static boolean matchEnd(IElementType start, boolean advanced,  PsiBuilder builder, int level, TokenSet skips, TokenSet types) {
        if (!advanced)
            builder.advanceLexer();
        IElementType type = builder.getTokenType();

        while (true) {
            if (type == null || builder.eof()) {
                return false;
            }

            if (types.contains(type)) {
                if (level != 0)
                    builder.advanceLexer();
                return true;
            }

            while (!skips.contains(type)) {
                boolean isMatched = matchStart(builder, level + 1, type, false);
                if (!isMatched || level == 0)
                    break;
                type = builder.getTokenType();
            }

            type = builder.getTokenType();
            if (types.contains(type)) {
                if (level != 0)
                    builder.advanceLexer();
                return true;
            }

            builder.advanceLexer();
            type = builder.getTokenType();
        }
    }
}
