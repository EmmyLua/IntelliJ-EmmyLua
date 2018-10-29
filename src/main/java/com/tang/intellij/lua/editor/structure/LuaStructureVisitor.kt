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
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.comment.psi.LuaDocTagField
import com.tang.intellij.lua.comment.psi.LuaDocVisitor
import com.tang.intellij.lua.psi.*
import java.util.*


private class LexicalContext(val element: LuaTreeElement? = null, val parent: LexicalContext? = null) {
    val children = LinkedHashMap<String, LuaTreeElement>()

    fun addChildContext(e: LuaTreeElement, addChild: Boolean = true): LexicalContext {
        if (addChild) {
            element?.addChild(e)
            children[e.name] = e
        }

        return LexicalContext(e, this)
    }

    fun addChild(e: LuaTreeElement, name: String? = null) {
        element?.addChild(e)
        children[name ?: e.name] = e
    }

    fun findElementNamed(name: String): LuaTreeElement? {
        return children[name] ?: parent?.findElementNamed(name)
    }
}

class LuaStructureVisitor : LuaVisitor() {
    private var current: LexicalContext? = LexicalContext()

    private fun pushContext(e: LuaTreeElement, addChild: Boolean = true) {
        current = current?.addChildContext(e, addChild)
    }

    private fun popContext() {
        current = current?.parent
    }

    private fun findElementNamed(name: String?): LuaTreeElement? {
        if (name == null) return null
        return current?.findElementNamed(name)
    }

    private fun addChild(child: LuaTreeElement, name: String? = null) {
        current?.addChild(child, name)
    }

    fun getChildren(): Array<TreeElement> {
        return current?.children?.values?.toTypedArray<TreeElement>() ?: emptyArray()
    }

    /**
     * Get the class/field declarations present in a document comment
     */
    private fun getDocCommentDeclarations(commentOwner: LuaCommentOwner): ArrayList<LuaTreeElement>? {
        val comment = commentOwner.comment ?: return null

        val elements = ArrayList<LuaTreeElement>()

        comment.acceptChildren(object : LuaDocVisitor() {
            override fun visitTagClass(o: LuaDocTagClass) {
                elements.add(LuaClassElement(o))
            }

            override fun visitTagField(o: LuaDocTagField) {
                elements.add(LuaClassFieldElement(o))
            }
        })

        return elements
    }

    override fun visitAssignStat(o: LuaAssignStat) {
        val variableNames = o.varExprList.exprList
        val expressions = o.valueExprList?.exprList

        // We're only interested in named entities
        repeat(variableNames.size) { i ->
            val expr = expressions?.getOrNull(i)
            val nameExpr = variableNames[i]
            val name = nameExpr.name ?: return

            if (nameExpr is PsiNamedElement && findElementNamed(name) != null) {
                // We're assigning to a previously declared entity -- ignore
                return
            }

            var owner: LuaTreeElement? = null
            if (nameExpr is LuaIndexExpr) {
                owner = handleCompoundName(nameExpr.prefixExpr) ?: return
            }

            val child = if (expr is LuaClosureExpr) {
                when (owner) {
                    is LuaClassElement -> LuaClassMethodElement(nameExpr, name, expr.paramSignature)
                    is LuaLocalVarElement -> LuaLocalFuncElement(nameExpr, name, expr.paramSignature)
                    else -> LuaGlobalFuncElement(nameExpr, name, expr.paramSignature)
                }
            } else {
                when (owner) {
                    is LuaClassElement -> LuaClassFieldElement(o, name)
                    is LuaLocalVarElement -> LuaLocalVarElement(o, name)
                    else -> {
                        val declarations = getDocCommentDeclarations(o)

                        val names = o.varExprList.exprList
                        val exprs = o.valueExprList?.exprList

                        for (idx in 0 until names.size) {
                            val declaration = declarations?.getOrNull(idx)
                            val valueExpr = exprs?.getOrNull(idx)
                            val nameExpr2 = names[idx]

                            var exprOwner: LuaTreeElement? = null
                            if (declaration is LuaClassElement) {
                                exprOwner = declaration

                                addChild(declaration, nameExpr2.name)
                            } else if (nameExpr2 is LuaNameExpr) {
                                exprOwner = LuaLocalVarElement(nameExpr2)

                                addChild(exprOwner)
                            }

                            if (exprOwner != null && valueExpr is LuaTableExpr) {
                                handleTableExpr(valueExpr, exprOwner)
                            }
                        }

                        LuaGlobalVarElement(o, name)
                    }
                }
            }

            if (owner != null) {
                owner.addChild(child)
            } else {
                addChild(child)
            }

            if (expr is LuaClosureExpr) {
                pushContext(child, false)
                expr.funcBody.accept(this)
                popContext()
            } else if (expr is LuaTableExpr) {
                handleTableExpr(expr, child)
            }
        }
    }

    override fun visitFuncDef(o: LuaFuncDef) {
        addChild(LuaGlobalFuncElement(o))
    }

    override fun visitExprStat(o: LuaExprStat) {
        val callExpr = o.expr as? LuaCallExpr

        val args = callExpr?.args as? LuaListArgs

        args?.exprList?.forEach{ arg ->
            if (arg is LuaClosureExpr) {
                val elem = LuaLocalFuncElement(arg, "<anonymous>", arg.paramSignature)

                pushContext(elem)

                arg.funcBody.accept(this)

                popContext()
            }
        }
    }

    private fun handleTableExpr(o: LuaTableExpr, exprOwner: LuaTreeElement) {
        o.tableFieldList.forEach { tableField ->
            val name = tableField.name

            if (name != null) {
                val expr = tableField.exprList[0]

                val child = if (expr is LuaClosureExpr) {
                    if (exprOwner is LuaClassElement) {
                        LuaClassMethodElement(tableField, name, expr.paramSignature, tableField.visibility)
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

    override fun visitFuncBody(o: LuaFuncBody) {
        // A func body has, as _children, some number of param name defs followed by a block
        PsiTreeUtil.getChildOfType(o, LuaBlock::class.java)?.acceptChildren(this)
    }

    override fun visitLocalDef(o: LuaLocalDef) {
        val nameList = o.nameList ?: return

        val declarations: ArrayList<LuaTreeElement>? = getDocCommentDeclarations(o)

        val names = nameList.nameDefList
        val exprs = o.exprList?.exprList

        for (idx in 0 until names.size) {
            val declaration = declarations?.getOrNull(idx)
            val valueExpr = exprs?.getOrNull(idx)
            val nameDef = names[idx]

            val exprOwner: LuaTreeElement
            if (declaration is LuaClassElement) {
                exprOwner = declaration

                addChild(declaration, nameDef.name)
            } else {
                exprOwner = LuaLocalVarElement(nameDef)

                addChild(exprOwner)
            }

            if (valueExpr is LuaTableExpr) {
                handleTableExpr(valueExpr, exprOwner)
            }
        }
    }

    override fun visitLocalFuncDef(o: LuaLocalFuncDef) {
        pushContext(LuaLocalFuncElement(o))

        o.funcBody?.accept(this)

        popContext()
    }

    private fun getNamePartsFromCompoundName(namePartExpr: LuaExpr): ArrayList<LuaExpr> {
        val result = ArrayList<LuaExpr>()

        var namePart = namePartExpr
        while (true) {
            result.add(namePart)
            namePart = namePart.firstChild as? LuaExpr ?: break
        }
        result.reverse()

        return result
    }

    // return null if namePartExpr contains indexExpr such as `b[1]`
    private fun handleCompoundName(namePartExpr: LuaExpr, parent: LuaTreeElement? = null): LuaTreeElement? {
        val nameParts = getNamePartsFromCompoundName(namePartExpr)

        var element: LuaTreeElement? = null

        for (namePart in nameParts) {
            var child: LuaTreeElement?
            // fix crash : a.b[1].c = 2
            val name = namePart.name ?: return null

            if (element == null) {
                child = findElementNamed(name)

                if (child == null) {
                    child = if (parent == null) {
                        LuaGlobalVarElement(namePart)
                    } else {
                        LuaLocalVarElement(namePart)
                    }

                    addChild(child)
                }
            } else {
                child = element.childNamed(name)

                if (child == null) {
                    child = LuaLocalVarElement(namePart)

                    element.addChild(child)
                }
            }

            element = child
        }

        return element
    }

    override fun visitClassMethodDef(o: LuaClassMethodDef) {
        handleCompoundName(o.classMethodName.expr)?.let { treeElem->
            val elem = LuaClassMethodElement(o, o.visibility)

            treeElem.addChild(elem)

            val funcBody = o.funcBody
            if (funcBody != null) {
                pushContext(elem, false)

                funcBody.accept(this)

                popContext()
            }
        }
    }

    private fun compressChild(element: TreeElement) {
        if (element !is LuaVarElement) {
            return
        }

        val children = element.children
        if (children.size == 1) {
            if (children.first() is LuaVarElement) {
                val child = children.first() as LuaTreeElement

                element.name += "." + child.name

                element.clearChildren()

                child.children.forEach { childElem -> element.addChild(childElem as LuaTreeElement) }

                compressChild(element)
            }
        } else {
            children.forEach { childElem -> compressChild(childElem) }
        }
    }

    fun compressChildren() {
        current?.children?.values?.forEach { element -> compressChild(element) }
    }
}
