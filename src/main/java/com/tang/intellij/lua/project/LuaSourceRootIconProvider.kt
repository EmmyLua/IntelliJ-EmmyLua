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

package com.tang.intellij.lua.project

import com.intellij.ide.IconLayerProvider
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiDirectory
import com.intellij.util.PlatformIcons
import javax.swing.Icon

class LuaSourceRootIconProvider : IconLayerProvider {
    override fun getLayerIcon(element: Iconable, isLocked: Boolean): Icon? {
        if (element is PsiDirectory) {
            if (LuaSourceRootManager.getInstance(element.project).isSourceRoot(element.virtualFile)) {
                return PlatformIcons.PUBLIC_ICON
            }
        }
        return null
    }

    override fun getLayerDescription(): String {
        return "Lua source root"
    }
}