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

/**
 * 表达式，可以推算计算后的数据类型
 * Created by TangZX on 2016/12/2.
 */
public interface LuaExpression extends LuaPsiElement {

    // 表达式计算后的结果推算
    @NotNull
    ITy guessType(SearchContext context);
}
