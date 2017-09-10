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
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef
import com.tang.intellij.lua.comment.psi.LuaDocVisitor
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*
import java.util.*
import javax.swing.Icon

/**

 * Created by TangZX on 2016/12/13.
 */
class LuaFileElement(private val file: LuaFile) : StructureViewTreeElement {

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
        val list = ArrayList<TreeElement>()

        file.acceptChildren(object : LuaVisitor() {

            internal fun visitDocComment(comment: PsiElement) {
                comment.acceptChildren(object : LuaDocVisitor() {
                    override fun visitClassDef(o: LuaDocClassDef) {
                        list.add(LuaClassElement(o))
                    }

                    override fun visitFieldDef(o: LuaDocFieldDef) {
                        list.add(LuaClassFieldElement(o))
                    }
                })
            }

            override fun visitAssignStat(o: LuaAssignStat) {
                super.visitAssignStat(o)
                list.add(LuaAssignElement(o))
            }

            override fun visitFuncDef(o: LuaFuncDef) {
                list.add(LuaFuncElement(o))
            }

            override fun visitElement(element: PsiElement?) {
                if (element is LuaCommentOwner) {
                    val comment = element.comment
                    if (comment != null)
                        visitDocComment(comment)
                }
            }

            override fun visitLocalDef(o: LuaLocalDef) {
                val comment = o.comment
                if (comment != null)
                    visitDocComment(comment)
                else
                    list.add(LuaLocalElement(o))
            }

            override fun visitLocalFuncDef(o: LuaLocalFuncDef) {
                list.add(LuaLocalFuncElement(o))
            }

            override fun visitClassMethodDef(o: LuaClassMethodDef) {
                list.add(LuaClassMethodElement(o))
            }
        })

        return list.toTypedArray()
    }
}