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

import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * #local function
 * #function
 * #lambda function
 * #class method
 *
 * Created by TangZX on 2016/12/9.
 */
public interface LuaFuncBodyOwner extends LuaParametersOwner, LuaTypeGuessable {
    @Nullable
    LuaFuncBody getFuncBody();

    /**
     * 返回类型
     */
    @NotNull
    ITy guessReturnType(SearchContext searchContext);

    @Nullable
    default ITy getVarargType() {
        return LuaPsiImplUtilKt.getVarargTy(this);
    }

    @NotNull
    LuaParamInfo[] getParams();

    default String getParamSignature() {
        return LuaPsiImplUtilKt.getParamSignature(this);
    }
}
