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

package com.tang.intellij.lua.editor.structure

import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*

/**
 * Created by TangZX on 2016/12/13.
 */
class LuaFileElement(private val file: LuaPsiFile) : LuaTreeElement(file, file.name, LuaIcons.FILE) {
    override fun getChildren(): Array<TreeElement> {
        val visitor = LuaStructureVisitor()

        file.acceptChildren(visitor)

        visitor.compressChildren()

        return visitor.getChildren()
    }
}
