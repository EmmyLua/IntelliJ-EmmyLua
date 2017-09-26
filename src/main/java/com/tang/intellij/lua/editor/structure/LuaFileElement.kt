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

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*
import javax.swing.Icon

/**
 * Created by TangZX on 2016/12/13.
 */
class LuaFileElement(private val file:LuaFile) : StructureViewTreeElement {

    override fun getValue(): Any {
        return file
    }

    override fun navigate(b: Boolean) {

    }

    override fun canNavigate(): Boolean {
        return false
    }

    override fun canNavigateToSource(): Boolean {
        return false
    }

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String? {
                return file.name
            }

            override fun getLocationString(): String? {
                return file.name
            }

            override fun getIcon(b: Boolean): Icon? {
                return LuaIcons.FILE
            }
        }
    }

    override fun getChildren(): Array<TreeElement> {
        val visitor = LuaStructureVisitor()

        file.acceptChildren(visitor)

        // Eliminate empty middle classes
        visitor.compressChildren()

        return visitor.children.toTypedArray()
    }
}
