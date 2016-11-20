package com.tang.intellij.lua.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;

public class LuaParserUtil extends GeneratedParserUtilBase {

    public static boolean twoExpr(PsiBuilder builder_, int level_, Parser parser) {
        PsiBuilder.Marker marker = builder_.mark();
        boolean r = parser.parse(builder_, level_);
        r = r && parser.parse(builder_, level_);
        marker.rollbackTo();
        return r;
    }
}
