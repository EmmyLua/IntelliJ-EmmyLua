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

package com.tang.intellij.lua.index

import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.tang.intellij.lua.lang.LuaLanguage

class LuaPsiTreeChangeListener(val manager: IndexManager) : PsiTreeChangeAdapter() {

    override fun beforeChildRemoval(event: PsiTreeChangeEvent) {
        val child = event.child
        if (child.language == LuaLanguage.INSTANCE)
            manager.remove(child)
    }

    override fun beforeChildReplacement(event: PsiTreeChangeEvent) {
        val child = event.oldChild
        if (child.language == LuaLanguage.INSTANCE)
            manager.remove(child)
    }

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        event.file?.let { manager.onFileUpdate(it) }
    }
}