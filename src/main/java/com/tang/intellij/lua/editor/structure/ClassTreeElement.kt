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
import com.tang.intellij.lua.psi.LuaPsiElement
import java.util.ArrayList

class ClassTreeElement(indexExpr:LuaPsiElement) : LuaTreeElement(indexExpr, LuaIcons.CLASS) {
    val children = ArrayList<TreeElement>()
    var name = element.name

    override fun getPresentableText(): String? {
        return name
    }

    fun addChild(child: TreeElement) {
        children.add(child)
    }

    override fun getChildren(): Array<TreeElement> {
        return children.toTypedArray()
    }

    fun getChildList(): ArrayList<TreeElement> {
        return children
    }
}
