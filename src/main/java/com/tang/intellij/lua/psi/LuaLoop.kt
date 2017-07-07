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

package com.tang.intellij.lua.psi

import com.intellij.psi.PsiElement

/**
 * for, repeat, while
 * Created by Administrator on 2017/6/28.
 */
interface LuaLoop : LuaPsiElement


val LuaLoop.head:PsiElement? get() {
    val type = when (this) {
        is LuaWhileStat -> LuaTypes.WHILE
        is LuaRepeatStat -> LuaTypes.REPEAT
        else -> LuaTypes.FOR
    }
    val headNode = node.findChildByType(type)
    return headNode?.psi
}
val LuaLoop.end:PsiElement? get() {
    val type = when (this) {
        is LuaRepeatStat -> LuaTypes.UNTIL
        else -> LuaTypes.END
    }
    val endNode = node.findChildByType(type)
    return endNode?.psi
}
