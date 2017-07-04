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

package com.tang.intellij.lua.comment;

import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.psi.LuaDocPsiElement;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaCommentUtil {

    @Nullable
    public static LuaCommentOwner findOwner(LuaDocPsiElement element) {
        LuaComment comment = findContainer(element);
        if (comment.getParent() instanceof LuaCommentOwner)
            return (LuaCommentOwner) comment.getParent();
        return null;
    }

    @NotNull
    public static LuaComment findContainer(@NotNull LuaDocPsiElement element) {
        while (true) {
            if (element instanceof LuaComment) {
                return (LuaComment) element;
            }
            element = (LuaDocPsiElement) element.getParent();
        }
    }

    @Nullable
    public static LuaComment findComment(LuaCommentOwner element) {
        return PsiTreeUtil.getChildOfType(element,  LuaComment.class);
    }
}
