/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tang.intellij.lua.codeInsight.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.tang.intellij.lua.psi.LuaBlock;
import com.tang.intellij.lua.psi.LuaLocalDef;
import com.tang.intellij.lua.psi.LuaLocalFuncDef;

/**
 *
 * Created by TangZX on 2017/2/8.
 */
abstract class EmptyBodyBase extends LocalInspectionTool {

    private static Class[] invalidClasses = new Class[] {
            PsiWhiteSpace.class,
            PsiComment.class,
            LuaLocalFuncDef.class,
            LuaLocalDef.class
    };

    private static boolean isValid(PsiElement element) {
        for (Class invalidClass : invalidClasses) {
            if (invalidClass.isInstance(element)) {
                return false;
            }
        }
        return true;
    }

    boolean isValidBlock(LuaBlock block) {
        if (block != null) {
            PsiElement child = block.getFirstChild();
            boolean hasValid = false;
            while (child != null) {
                if (isValid(child)) {
                    hasValid = true;
                    break;
                }
                child = child.getNextSibling();
            }
            return hasValid;
        }
        return true;
    }
}
