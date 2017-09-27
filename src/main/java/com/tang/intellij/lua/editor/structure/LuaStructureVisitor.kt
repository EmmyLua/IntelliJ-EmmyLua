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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef
import com.tang.intellij.lua.comment.psi.LuaDocVisitor
import com.tang.intellij.lua.psi.*
import java.util.*


private class ClassInfo(val treeElement:LuaClassElement, clsName:String? = null, varName:String? = null) {
    val className:String = clsName ?: varName ?: treeElement.className
    val variableName:String = varName ?: className

    val nestedClasses = HashMap<String, ClassInfo>()

    operator fun get(name:String):ClassInfo? {
        return nestedClasses[name]
    }

    operator fun set(name:String, elem:ClassInfo) {
        nestedClasses[name] = elem

        treeElement.addChild(elem.treeElement)
    }

    fun addMethod(mtd:LuaFuncElement) {
        treeElement.addChild(mtd)
    }

    fun addField(fld:LuaClassFieldElement) {
        treeElement.addChild(fld)
    }
}


private class Context(val element:LuaTreeElement?=null, val parent:Context?=null) {
    val locals = HashMap<String, Int>()
    val classes = HashMap<String, ClassInfo>()
    private var children:ArrayList<TreeElement>? = null

    init {
        if (element == null) {
            children = ArrayList()
        }
    }

    fun getChildren():Array<TreeElement> {
        return element?.children ?: children!!.toTypedArray()
    }

    fun addChild(l:LuaTreeElement) {
        if (l is LuaNameDefElement) {
            locals.put(l.nameDef.name, element?.children?.size ?: children!!.size)
        }

        element?.addChild(l) ?: children!!.add(l)
    }

    fun getChildAt(i:Int):LuaTreeElement {
        return (element?.children?.get(i) ?: children!![i]) as LuaTreeElement
    }

    fun setChildAt(i:Int, e:LuaTreeElement) {
        if (element != null) {
            element.children[i] = e
        } else {
            children!![i] = e
        }
    }

    fun addClass(classInfo:ClassInfo) {
        classes.put(classInfo.variableName, classInfo)

        element?.addChild(classInfo.treeElement) ?: children!!.add(classInfo.treeElement)
    }

    fun findClassNamed(name:String):ClassInfo? {
        return classes[name] ?: parent?.findClassNamed(name)
    }

    fun findLocalNamed(name:String):Pair<Context, Int>? {
        val local = locals[name]

        return if (local != null) Pair(this, local) else parent?.findLocalNamed(name)
    }

    fun removeLocalNamed(name:String) {
        if (name in locals) {
            locals.remove(name)
        } else {
            parent?.removeLocalNamed(name)
        }
    }
}


class LuaStructureVisitor : LuaVisitor() {
    private val contextStack = Stack<Context>()

    init {
        contextStack.push(Context())
    }

    fun getChildren():Array<TreeElement> {
        return getCurContext().getChildren()
    }

    private fun getCurContext():Context {
        return contextStack.peek()
    }

    private fun pushContext(element:LuaTreeElement, addElement:Boolean=true) {
        val newContext = Context(element, getCurContext())

        if (addElement) {
            getCurContext().addChild(element)
        }

        contextStack.push(newContext)
    }

    private fun popContext() {
        contextStack.pop()
    }

    private fun getDocCommentDeclarations(commentOwner: LuaCommentOwner):ArrayList<LuaTreeElement>? {
        val comment = commentOwner.comment

        if (commentOwner.comment == null) {
            return null
        }

        val elements = ArrayList<LuaTreeElement>()

        comment.acceptChildren(object : LuaDocVisitor() {
            override fun visitClassDef(o: LuaDocClassDef) {
                elements.add(LuaClassElement(o))
            }

            override fun visitFieldDef(o: LuaDocFieldDef) {
                elements.add(LuaClassFieldElement(o))
            }
        })

        return elements
    }

    override fun visitFile(file: PsiFile?) {
        pushContext(LuaFileElement(file as LuaFile))

        file.acceptChildren(this)

        popContext()
    }

    override fun visitAssignStat(o: LuaAssignStat) {
        val variableNames = o.varExprList
        val expressions = o.valueExprList

        // We're only interested in named entities
        repeat(variableNames.children.size) {i ->
            val expr = expressions?.children?.get(i)
            val name  = variableNames.children[i] as LuaPsiElement

            val classInfo:ClassInfo?
            classInfo = when (name) {
                is LuaIndexExpr -> handleCompoundName(name)
                is LuaNameExpr -> getCurContext().findClassNamed(name.name)
                else -> getCurContext().findClassNamed((name as LuaNameDef).name)
            }

            if (classInfo != null) {
                if (expr is LuaClosureExpr) {
                    classInfo.addMethod(LuaFuncElement.asClassMethod(name, name.name!!, expr.paramSignature))
                } else {
                    classInfo.addField(LuaClassFieldElement(o, name.name!!))
                }
            } else {
                if (expr is LuaClosureExpr) {
                    getCurContext().addChild(LuaFuncElement(name, name.name, expr.paramSignature))
                } else {
                    getCurContext().addChild(LuaAssignElement(o))
                }
            }
        }
    }

    override fun visitFuncDef(o: LuaFuncDef) {
        getCurContext().addChild(LuaFuncElement(o))
    }

    override fun visitElement(element: PsiElement?) {
        if (element is LuaCommentOwner) {
            getDocCommentDeclarations(element)
        }
    }

    override fun visitTableExpr(o: LuaTableExpr) {
        super.visitTableExpr(o)
    }

    private fun handleTableExpr(o:LuaTableExpr, exprOwner:LuaTreeElement?=null) {
        o.tableFieldList.forEach{tableField ->
            val name = tableField.name

            if (name != null) {
                val expr = tableField.exprList[0]

                if (expr is LuaClosureExpr) {
                    exprOwner?.addChild(LuaFuncElement.asClassMethod(tableField, name, expr.paramSignature))
                } else {
                    exprOwner?.addChild(LuaClassFieldElement(tableField, name))
                }
            }
        }
        super.visitTableExpr(o)
    }

    override fun visitFuncBody(o: LuaFuncBody) {
        // A func body has, as children, some number of param name defs followed by a block
        val block = o.children[o.children.size - 1]

        block.accept(this)
    }

    override fun visitBlock(o: LuaBlock) {
        o.statementList.forEach{s -> s.accept(this)}
    }

    override fun visitLocalDef(o: LuaLocalDef) {
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
                val classInfo = ClassInfo(declaration, declaration.className, nameDef.name)

                getCurContext().addClass(classInfo)
            } else {
                exprOwner = LuaNameDefElement(nameDef)
                getCurContext().addChild(exprOwner)
            }

            if (expr is LuaTableExpr) {
                handleTableExpr(expr, exprOwner)
            }
        }
    }

    override fun visitLocalFuncDef(o: LuaLocalFuncDef) {
        pushContext(LuaFuncElement(o))

        o.funcBody?.accept(this)

        popContext()
    }

    private fun handleCompoundName(owner: LuaPsiElement):ClassInfo {
        var namePart:LuaExpr
        namePart = (owner as? LuaClassMethodDef)?.classMethodName?.expr ?: owner.firstChild as LuaExpr

        while (namePart.firstChild is LuaExpr) {
            namePart = namePart.firstChild as LuaExpr
        }

        var curClassInfo:ClassInfo? = getCurContext().findClassNamed(namePart.lastChild.text)

        if (curClassInfo == null) {
            val name = namePart.lastChild.text
            val local = getCurContext().findLocalNamed(name)
            var idx:Int? = null
            var context:Context? = null

            val curClassElem:LuaClassElement?

            if (local != null) {
                context = local.first
                idx = local.second

                context.removeLocalNamed(name)

                val nameDefEle:LuaNameDefElement = context.getChildAt(idx) as LuaNameDefElement

                curClassElem = LuaClassElement(nameDefEle.nameDef)
            } else {
                curClassElem = LuaClassElement(namePart as LuaPsiElement)
            }

            curClassInfo = ClassInfo(curClassElem)

            if (idx != null) {
                context?.setChildAt(idx, curClassElem)
            } else {
                getCurContext().addClass(curClassInfo)
            }
        }

        while (namePart.parent != owner) {
            namePart = namePart.parent as LuaExpr
            val name = namePart.lastChild.text

            var childClassInfo = curClassInfo!![name]

            if (childClassInfo == null) {
                val curClassElem = LuaClassElement(namePart as LuaPsiElement)

                childClassInfo = ClassInfo(curClassElem, curClassElem.className)

                curClassInfo[curClassElem.className] = childClassInfo
            }

            curClassInfo = childClassInfo
        }

        return curClassInfo!!
    }

    override fun visitClassMethodDef(o: LuaClassMethodDef) {
        val classStruct = handleCompoundName(o.classMethodName)

        val elem = LuaFuncElement(o)

        classStruct.addMethod(elem)

        val funcBody = o.funcBody
        if (funcBody != null) {
            pushContext(elem, false)

            funcBody.accept(this)

            popContext()
        }
    }
//
//    private fun compressChild(element:TreeElement) {
//        if (element !is ClassTreeElement) {
//            return
//        }
//
//        if (element.children.size == 1) {
//            if (element.children[0] is ClassTreeElement) {
//                val child = element.children[0] as ClassTreeElement
//
//                element.name += "." + child.name
//
//                element.getChildList().clear()
//
//                child.children.forEach{childElem -> element.addChild(childElem)}
//
//                compressChild(element)
//            }
//        } else {
//            element.children.forEach{childElem -> compressChild(childElem)}
//        }
//    }
//
//    fun compressChildren() {
//        children.forEach{elem -> compressChild(elem)}
//    }
}
