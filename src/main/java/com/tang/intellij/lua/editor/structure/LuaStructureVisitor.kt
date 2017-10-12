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
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef
import com.tang.intellij.lua.comment.psi.LuaDocVisitor
import com.tang.intellij.lua.psi.*
import java.util.*


private class LexicalContext(val element:LuaTreeElement?=null, val parent:LexicalContext?=null) {
    val children = LinkedHashMap<String, LuaTreeElement>()

    fun addChildContext(e:LuaTreeElement, addChild:Boolean=true):LexicalContext {
        if (addChild) {
            element?.addChild(e)
            children[e.name] = e
        }

        return LexicalContext(e, this)
    }

    fun addChild(e:LuaTreeElement, name:String?=null) {
        element?.addChild(e)
        children[name ?: e.name] = e
    }

    fun findElementNamed(name:String):LuaTreeElement? {
        return children[name] ?: parent?.findElementNamed(name)
    }
}

class LuaStructureVisitor : LuaVisitor() {
    private var current:LexicalContext? = LexicalContext()

    private fun pushContext(e:LuaTreeElement, addChild:Boolean=true) {
        current = current?.addChildContext(e, addChild)
    }

    private fun popContext() {
        current = current?.parent
    }

    private fun findElementNamed(name:String):LuaTreeElement? {
        return current?.findElementNamed(name)
    }

    private fun addChild(child:LuaTreeElement, name:String?=null) {
        current?.addChild(child, name)
    }

    fun getChildren():Array<TreeElement> {
        return current?.children?.values?.toTypedArray<TreeElement>() ?: emptyArray()
    }

    /**
     * Get the class/field declarations present in a document comment
     */
    private fun getDocCommentDeclarations(commentOwner:LuaCommentOwner):ArrayList<LuaTreeElement>? {
        val comment = commentOwner.comment

        if (commentOwner.comment == null) {
            return null
        }

        val elements = ArrayList<LuaTreeElement>()

        comment.acceptChildren(object:LuaDocVisitor() {
            override fun visitClassDef(o:LuaDocClassDef) {
                elements.add(LuaClassElement(o))
            }

            override fun visitFieldDef(o:LuaDocFieldDef) {
                elements.add(LuaClassFieldElement(o))
            }
        })

        return elements
    }

    override fun visitAssignStat(o:LuaAssignStat) {
        val variableNames = o.varExprList
        val expressions = o.valueExprList

        // We're only interested in named entities
        repeat(variableNames.children.size) {i ->
            val expr = expressions?.children?.get(i)
            val name  = variableNames.children[i] as LuaPsiElement

            if (name is LuaNameExpr && findElementNamed(name.name) != null || name is LuaNameDef && findElementNamed(name.name) != null) {
                // We're assigning to a previously declared entity -- ignore
                return
            }

            val owner:LuaTreeElement? = if (name is LuaIndexExpr) handleCompoundName(name) else null

            val child = if (expr is LuaClosureExpr) {
                            when (owner) {
                                is LuaClassElement -> LuaClassMethodElement(name, name.name!!, expr.paramSignature)
                                is LuaLocalVarElement -> LuaLocalFuncElement(name, name.name!!, expr.paramSignature)
                                else -> LuaGlobalFuncElement(name, name.name!!, expr.paramSignature)
                            }
                        } else {
                            when (owner) {
                                is LuaClassElement -> LuaClassFieldElement(o, name.name!!)
                                is LuaLocalVarElement -> LuaLocalVarElement(o, name.name!!)
                                else -> {
                                    val declarations = getDocCommentDeclarations(o)

                                    val names = o.varExprList.exprList
                                    val exprs = o.valueExprList?.exprList

                                    for (idx in 0 until names.size) {
                                        val declaration = if (declarations == null || idx >= declarations.size) null else declarations[idx]
                                        val expr = if (exprs == null || idx >= exprs.size) null else exprs[idx]
                                        val nameDef = names[idx] as LuaNameExpr

                                        val exprOwner:LuaTreeElement
                                        if (declaration is LuaClassElement) {
                                            exprOwner = declaration

                                            addChild(declaration, nameDef.name)
                                        } else {
                                            exprOwner = LuaLocalVarElement(nameDef)

                                            addChild(exprOwner)
                                        }

                                        if (expr is LuaTableExpr) {
                                            handleTableExpr(expr, exprOwner)
                                        }
                                    }

                                    LuaGlobalVarElement(o, name.name!!)
                                }
                            }
                        }

            if (owner != null) {
                owner.addChild(child)
            } else {
                addChild(child)
            }

            if (expr is LuaClosureExpr) {
                expr.accept(this)
            } else if (expr is LuaTableExpr) {
                handleTableExpr(expr, child)
            }
        }
    }

    override fun visitFuncDef(o:LuaFuncDef) {
        addChild(LuaGlobalFuncElement(o))
    }

    private fun handleTableExpr(o:LuaTableExpr, exprOwner:LuaTreeElement) {
        o.tableFieldList.forEach{tableField ->
            val name = tableField.name

            if (name != null) {
                val expr = tableField.exprList[0]

                val child = if (expr is LuaClosureExpr) {
                                if (exprOwner is LuaClassElement) {
                                    LuaClassMethodElement(tableField, name, expr.paramSignature)
                                } else {
                                    LuaLocalFuncElement(tableField, name, expr.paramSignature)
                                }
                            } else {
                                if (exprOwner is LuaClassElement) {
                                    LuaClassFieldElement(tableField, name)
                                } else {
                                    LuaLocalVarElement(tableField, name)
                                }
                            }

                exprOwner.addChild(child)
            }
        }

        super.visitTableExpr(o)
    }

    override fun visitFuncBody(o:LuaFuncBody) {
        // A func body has, as _children, some number of param name defs followed by a block
        val block = o.children[o.children.size - 1]

        block.accept(this)
    }

    override fun visitBlock(o:LuaBlock) {
        o.statementList.forEach{s -> s.accept(this)}
    }

    override fun visitLocalDef(o:LuaLocalDef) {
        val nameList = o.nameList ?: return

        val declarations:ArrayList<LuaTreeElement>? = getDocCommentDeclarations(o)

        val names = nameList.nameDefList
        val exprs = o.exprList?.exprList

        for (idx in 0 until names.size) {
            val declaration = if (declarations == null || idx >= declarations.size) null else declarations[idx]
            val expr = if (exprs == null || idx >= exprs.size) null else exprs[idx]
            val nameDef = names[idx] as LuaNameDef

            val exprOwner:LuaTreeElement
            if (declaration is LuaClassElement) {
                exprOwner = declaration

                addChild(declaration, nameDef.name)
            } else {
                exprOwner = LuaLocalVarElement(nameDef)

                addChild(exprOwner)
            }

            if (expr is LuaTableExpr) {
                handleTableExpr(expr, exprOwner)
            }
        }
    }

    override fun visitLocalFuncDef(o:LuaLocalFuncDef) {
        pushContext(LuaLocalFuncElement(o))

        o.funcBody?.accept(this)

        popContext()
    }

    private fun getNamePartsFromCompoundName(compoundNameParent:LuaPsiElement):ArrayList<LuaExpr> {
        val result = ArrayList<LuaExpr>()

        val secondToLast = (compoundNameParent as? LuaClassMethodDef)?.classMethodName?.expr ?: compoundNameParent.firstChild as LuaExpr
        var namePart:LuaExpr = secondToLast

        while (namePart.firstChild is LuaExpr) {
            namePart = namePart.firstChild as LuaExpr
        }

        result.add(namePart)

        while (namePart != secondToLast) {
            namePart = namePart.parent as LuaExpr
            result.add(namePart)
        }

        return result
    }

    private fun handleCompoundName(owner:LuaPsiElement, parent:LuaTreeElement?=null):LuaTreeElement {
        val nameParts = getNamePartsFromCompoundName(owner)

        var element:LuaTreeElement? = null

        nameParts.forEach{namePart ->
            var child:LuaTreeElement?
            if (element == null) {
                child = findElementNamed(namePart.name!!)

                if (child == null) {
                    child = if (parent == null) {
                                LuaGlobalVarElement(namePart)
                            } else {
                                LuaLocalVarElement(namePart)
                            }

                    addChild(child)
                }
            } else {
                child = element!!.childNamed(namePart.name!!)

                if (child == null) {
                    child = LuaLocalVarElement(namePart)

                    element!!.addChild(child)
                }
            }

            element = child
        }

        return element!!
    }

    override fun visitClassMethodDef(o:LuaClassMethodDef) {
        val treeElem = handleCompoundName(o.classMethodName/*, current?.element*/)

        val elem = LuaClassMethodElement(o)

        treeElem.addChild(elem)

        val funcBody = o.funcBody
        if (funcBody != null) {
            pushContext(elem, false)

            funcBody.accept(this)

            popContext()
        }
    }

    private fun compressChild(element:TreeElement) {
        if (element !is LuaVarElement) {
            return
        }

        if (element.children.size == 1) {
            if (element.children[0] is LuaVarElement) {
                val child = element.children[0] as LuaTreeElement

                element.name += "." + child.name

                element.clearChildren()

                child.children.forEach{childElem -> element.addChild(childElem as LuaTreeElement)}

                compressChild(element)
            }
        } else {
            element.children.forEach{childElem -> compressChild(childElem)}
        }
    }

    fun compressChildren() {
        current?.children?.values?.forEach{element -> compressChild(element)}
    }
}
