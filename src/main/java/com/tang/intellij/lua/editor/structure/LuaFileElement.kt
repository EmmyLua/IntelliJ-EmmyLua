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
        class ClassTreeElement(indexExpr:LuaPsiElement) : LuaTreeElement<LuaPsiElement>(indexExpr, LuaIcons.CLASS) {
            val children = ArrayList<TreeElement>()
            var name = element.name

            override fun getPresentableText(): String? {
                return name
            }

            fun addChild(child:TreeElement) {
                children.add(child)
            }

            override fun getChildren(): Array<TreeElement> {
                return children.toTypedArray()
            }

            fun getChildList():ArrayList<TreeElement> {
                return children
            }
        }

        class ShortMethodElement internal constructor(methodDef: LuaClassMethodDef) : LuaClassMethodElement(methodDef) {
            init {
                this.methodName = methodDef.name + methodDef.paramSignature
            }
        }

        class ClassStructure(val treeElement:TreeElement?) {
            val children = HashMap<String, ClassStructure>()

            operator fun get(name:String):ClassStructure? {
                return children[name]
            }

            operator fun set(name:String, elem:ClassStructure) {
                children[name] = elem

                if (treeElement != null && elem.treeElement != null) {
                    (treeElement as ClassTreeElement).addChild(elem.treeElement)
                }
            }

            fun addMethod(mtd:ShortMethodElement) {
                (treeElement as ClassTreeElement).addChild(mtd)
            }
        }

        class LuaNameDefElement internal constructor(val nameDef: LuaNameDef) : LuaTreeElement<LuaNameDef>(nameDef, LuaIcons.LOCAL_VAR) {
            override fun getPresentableText(): String? {
                return nameDef.name
            }
        }


        val locals = HashMap<String, Int>()
        val list = ArrayList<TreeElement>()
        val classes = ClassStructure(null)

        val visitor = object : LuaVisitor() {
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

            override fun visitCallStat(o: LuaCallStat) {
                val callExpr = o.firstChild
                val args = callExpr.lastChild
                val exprList = args.children[0]

                exprList.accept(this)
            }

            override fun visitExprList(o: LuaExprList) {
                o.exprList.forEach{it.accept(this)}
            }

            override fun visitTableExpr(o: LuaTableExpr) {
                super.visitTableExpr(o)
            }

            override fun visitClosureExpr(o: LuaClosureExpr) {
                o.children[0].accept(this)
            }

            override fun visitFuncBody(o: LuaFuncBody) {
                // A func body has, as children, some number of param name defs followed by a block
                val block = o.children[o.children.size - 1]

                block.accept(this)
            }

            override fun visitBlock(o: LuaBlock) {
                o.children.forEach{
                    if (it is LuaClassMethodDef) {
                        val classMethodName = it.classMethodName

                        var namePart = classMethodName.firstChild
                        while (namePart.firstChild is LuaExpr) {
                            namePart = namePart.firstChild
                        }

                        var curClassContext = classes

                        while (namePart != classMethodName) {
                            var name = namePart.lastChild.text

                            if (name == null) {
                                name = "<null>"
                            }

                            var curClassStruct = curClassContext[name]

                            if (curClassStruct == null) {
                                var curClassElem:ClassTreeElement? = null

                                if (curClassContext.treeElement == null && name in locals) {
                                    val idx = locals[name]

                                    if (idx != null) {
                                        locals.remove(name)
                                        val nameDefEle:LuaNameDefElement = list[idx] as LuaNameDefElement

                                        curClassElem = ClassTreeElement(nameDefEle.nameDef)

                                        list.removeAt(idx)
                                    }
                                }

                                if (curClassElem == null) {
                                    curClassElem = ClassTreeElement(namePart as LuaPsiElement)
                                }

                                curClassStruct = ClassStructure(curClassElem)

                                if (curClassContext.treeElement == null) {
                                    list.add(curClassElem)
                                }

                                curClassContext[name] = curClassStruct
                            }

                            curClassContext = curClassStruct
                            namePart = namePart.parent
                        }

                        curClassContext.addMethod(ShortMethodElement(it))

//                        list.add(LuaClassMethodElement(it))
                    } else {
                        it.accept(this)
                    }
                }
            }

            override fun visitClassMethod(o: LuaClassMethod) {
                super.visitClassMethod(o)
            }

            override fun visitLocalDef(o: LuaLocalDef) {
                val comment = o.comment
                if (comment != null)
                    visitDocComment(comment)
                else {
                    o.children[0].children.forEach{nameDef ->
                        locals[(nameDef as LuaNameDef).name] = list.size
                        list.add(LuaNameDefElement(nameDef))
                    }
//                    list.add(LuaLocalElement(o))
                }
            }

            override fun visitLocalFuncDef(o: LuaLocalFuncDef) {
                list.add(LuaLocalFuncElement(o))
            }

            override fun visitClassMethodDef(o: LuaClassMethodDef) {
                list.add(LuaClassMethodElement(o))
            }
        }

        file.acceptChildren(visitor)

        // Eliminate empty middle classes
        fun compressChild(element:TreeElement) {
            if (element !is ClassTreeElement) {
                return
            }

            if (element.children.size == 1) {
                if (element.children[0] is ClassTreeElement) {
                    val child = element.children[0] as ClassTreeElement

                    element.name += "." + child.name

                    element.getChildList().clear()

                    child.children.forEach{childElem -> element.addChild(childElem)}

                    compressChild(element)
                }
            } else {
                element.children.forEach{childElem -> compressChild(childElem)}
            }
        }

        list.forEach{elem -> compressChild(elem)}

        return list.toTypedArray()
    }
}
