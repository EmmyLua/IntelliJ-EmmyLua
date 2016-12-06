package com.tang.intellij.lua.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;

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
        PsiBuilder.Marker marker = builder_.mark();

        IElementType type = builder_.getTokenType();
        while (true) {
            if (type == null || builder_.eof() || type == LuaTypes.END) {
                break;
            }
            builder_.advanceLexer();
            type = builder_.getTokenType();
        }

        marker.collapse(LuaTypes.BLOCK);
        return true;
    }
}
