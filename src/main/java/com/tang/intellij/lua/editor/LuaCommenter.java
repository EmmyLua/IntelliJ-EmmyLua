package com.tang.intellij.lua.editor;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

/**
 * Lua Commenter
 * Created by TangZX on 2016/12/15.
 */
public class LuaCommenter implements Commenter {
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
}
