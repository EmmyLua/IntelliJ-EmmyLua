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
