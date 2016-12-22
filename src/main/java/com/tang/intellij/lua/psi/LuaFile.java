package com.tang.intellij.lua.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.lang.LuaFileType;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import org.jetbrains.annotations.NotNull;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaFile extends PsiFileBase {
    public LuaFile(@NotNull FileViewProvider fileViewProvider) {
        super(fileViewProvider, LuaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return LuaFileType.INSTANCE;
    }

    /**
     * 获取最后返回的类型
     * @return LuaTypeSet
     */
    public LuaTypeSet getReturnedType() {
        PsiElement lastChild = getLastChild();
        final LuaReturnStat[] last = {null};
        LuaPsiTreeUtil.walkTopLevelInFile(lastChild, LuaReturnStat.class, luaReturnStat -> {
            last[0] = luaReturnStat;
            return false;
        });
        LuaReturnStat lastReturn = last[0];
        if (lastReturn != null) {
            LuaExprList returnExpr = lastReturn.getExprList();
            if (returnExpr != null)
                return returnExpr.guessTypeAt(0);
        }
        return null;
    }
}
