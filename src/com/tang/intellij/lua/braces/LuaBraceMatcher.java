package com.tang.intellij.lua.braces;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Created by tangzx
 * Date : 2015/11/16.
 */
public class LuaBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(LuaTypes.LCURLY, LuaTypes.RCURLY, true),  //{}
            new BracePair(LuaTypes.LPAREN, LuaTypes.RPAREN, true),  //()
            new BracePair(LuaTypes.DO, LuaTypes.END, true),         //do end
            new BracePair(LuaTypes.IF, LuaTypes.END, true),         //if end
            new BracePair(LuaTypes.REPEAT, LuaTypes.UNTIL, true),   //if end
            new BracePair(LuaTypes.FUNCTION, LuaTypes.END, true)    //function end
    };


    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType iElementType, IElementType iElementType1) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile psiFile, int i) {
        return i;
    }
}