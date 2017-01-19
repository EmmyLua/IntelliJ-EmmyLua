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

package com.tang.intellij.lua.editor;

import com.intellij.codeInsight.generation.IndentedCommenter;
import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

/**
 * Lua Commenter
 * Created by TangZX on 2016/12/15.
 */
public class LuaCommenter implements Commenter, IndentedCommenter {
    @Nullable
    @Override
    public String getLineCommentPrefix() {
        return "--";
    }

    @Nullable
    @Override
    public String getBlockCommentPrefix() {
        return "--[[";
    }

    @Nullable
    @Override
    public String getBlockCommentSuffix() {
        return "]]";
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Nullable
    @Override
    public Boolean forceIndentedLineComment() {
        return true;
    }
}
