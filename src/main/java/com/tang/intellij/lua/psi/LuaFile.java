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

package com.tang.intellij.lua.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.lang.LuaFileType;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;
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
    public LuaTypeSet getReturnedType(SearchContext context) {
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
                return returnExpr.guessTypeAt(0, context);
        }
        return null;
    }
}
