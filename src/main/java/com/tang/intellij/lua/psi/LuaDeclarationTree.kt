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

package com.tang.intellij.lua.psi

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.util.BitUtil
import java.util.*

interface LuaDeclarationTree {
    companion object {
        private val key = Key.create<LuaDeclarationTree>("lua.object.tree.manager")
        fun get(file: PsiFile): LuaDeclarationTree {
            var ret = file.getUserData(key)
            if (ret != null && ret.shouldRebuild()) {
                file.putUserData(key, null)
                ret = null
            }
            if (ret == null) {
                var manager: LuaDeclarationTree? = null
                if (file is LuaPsiFile && !file.isContentsLoaded) {
                    manager = LuaDeclarationTreeStub(file)
                    try {
                        manager.buildTree(file)
                    } catch (e: Exception) {
                        manager = null
                    }
                }
                if (manager == null) {
                    manager = LuaDeclarationTreePsi(file)
                    manager.buildTree(file)
                }
                file.putUserData(key, manager)
                ret = manager
            }
            return ret
        }
    }

    interface IDeclaration {
        val name: String
        val psi: PsiNamedElement
        val isLocal: Boolean
        val isFunction: Boolean
        val isClassMember: Boolean
        val firstDeclaration: IDeclaration
    }

    fun shouldRebuild(): Boolean
    fun find(expr: LuaExpr): IDeclaration?
    fun walkUp(pin: PsiElement, process: (declaration: IDeclaration) -> Boolean)
    fun walkUpLocal(pin: PsiElement, process: (declaration: IDeclaration) -> Boolean) {
        walkUp(pin) {
            if (it.isLocal)
                process(it)
            else
                true
        }
    }
}

private abstract class Node {
    var next: Node? = null
    var prev: Node? = null
    abstract val pos: Int
}

private abstract class NodeContainer : Node() {
    protected val children = mutableListOf<Node>()
    private var _first: Node? = null
    private var _last: Node? = null

    fun add(child: Node) {
        children.add(child)
        if (_first == null)
            _first = child
        _last?.next = child
        child.prev = _last
        _last = child
    }

    fun <T> processNode(clazz: Class<T>, process: (node: T) -> Boolean): Boolean {
        var cur: Node? = _first
        while (cur != null) {
            if (clazz.isInstance(cur)) {
                if (!process(clazz.cast(cur)))
                    return false
            }
            cur = cur.next
        }
        return true
    }

    fun lastOrNull(predicate: (child: Node) -> Boolean): Node? {
        var cur = _last
        while (cur != null) {
            if (predicate(cur))
                return cur
            cur = cur.prev
        }
        return null
    }

    val first get() = _first

    val last get() = _last
}

private class Declaration(
        override val name: String,
        override val pos: Int,
        override val psi: PsiNamedElement,
        val flags: Int,
        val prevDeclaration: Declaration? = null
) : Node(), LuaDeclarationTree.IDeclaration {
    private val children = mutableMapOf<String, Declaration>()

    fun findField(name: String): Declaration? {
        return children[name]
    }

    fun addField(child: Declaration) {
        children[child.name] = child
    }

    override val firstDeclaration: Declaration get() = prevDeclaration?.firstDeclaration ?: this

    override val isLocal = BitUtil.isSet(flags, DeclarationFlag.Local)

    override val isFunction = BitUtil.isSet(flags, DeclarationFlag.Function)

    override val isClassMember = BitUtil.isSet(flags, DeclarationFlag.ClassMember)
}

private class DeclarationFlag {
    companion object {
        const val Local = 0x1
        const val Function = 0x2
        const val ClassMember = 0x4
        const val Global = 0x8
    }
}

private open class Scope(
        val tree: LuaDeclarationTreeBase,
        override val pos: Int,
        val parent: Scope? = null
) : NodeContainer() {

    open fun walkOver(process: (declaration: Declaration) -> Boolean): Boolean {
        return true
    }

    open fun walkUp(pos: Int, lev: Int, process: (declaration: Declaration) -> Boolean) {
        var cur: Node? = lastOrNull { it.pos < pos }
        while (cur != null) {
            if (cur is Declaration && !process(cur))
                return
            if (cur is Scope && !cur.walkOver(process))
                return
            cur = cur.prev
        }
        parent?.walkUp(this.pos, lev + 1, process)
    }

    private fun find(expr: LuaNameExpr): Declaration? {
        val name = expr.name
        var ret: Declaration? = null
        walkUp(tree.getPosition(expr), 0) { if (it.name == name) { ret = it; false } else true }
        return ret
    }

    fun find(expr: LuaExpr): Declaration? {
        if (expr is LuaNameExpr) {
            return find(expr)
        } else if (expr is LuaIndexExpr) {
            val fieldName = expr.name ?: expr.idExpr?.let { idExpr ->
                (idExpr as? LuaLiteralExpr)?.let { "[${it.text}]" }
            } ?: return null

            val declaration = find(expr.prefixExpr)
            return declaration?.findField(fieldName)
        }
        return null
    }
}

private abstract class LuaDeclarationTreeBase(val file: PsiFile) : LuaRecursiveVisitor(), LuaDeclarationTree {
    companion object {
        val scopeKey = Key.create<Scope>("lua.object.tree.scope")
    }

    val modificationStamp: Long = file.modificationStamp

    private val scopes = Stack<Scope>()
    private var topScope: Scope? = null
    private var curScope: Scope? = null

    override fun shouldRebuild(): Boolean {
        return modificationStamp != file.modificationStamp
    }

    private fun push(psi: LuaDeclarationScope): Scope {
        val pos = getPosition(psi)
        if (psi is LuaLocalDef) { // local a = a
            return push(object : Scope(this, pos, curScope) {
                override fun walkOver(process: (declaration: Declaration) -> Boolean): Boolean {
                    return processNode(Declaration::class.java, process)
                }

                override fun walkUp(pos: Int, lev: Int, process: (declaration: Declaration) -> Boolean) {
                    parent?.walkUp(this.pos, lev, process)
                }
            }, psi)
        }
        if (psi is LuaRepeatStat) { // repeat local a = false until a
            return push(object : Scope(this, pos, curScope) {
                override fun walkUp(pos: Int, lev: Int, process: (declaration: Declaration) -> Boolean) {
                    val blockScope = children.firstOrNull() as? Scope
                    if (lev == 0 && blockScope != null)
                        blockScope.walkUp(pos, lev, process)
                    else super.walkUp(pos, lev, process)
                }
            }, psi)
        }
        if (psi is LuaForBStat) { // for _, a in ipairs(a) do end
            return push(object : Scope(this, pos, curScope){
                override fun walkUp(pos: Int, lev: Int, process: (declaration: Declaration) -> Boolean) {
                    if (lev == 0) {
                        this.parent?.walkUp(pos, lev, process)
                    } else super.walkUp(pos, lev, process)
                }
            }, psi)
        }
        return push(Scope(this, pos, curScope), psi)
    }

    private fun push(scope: Scope, psi: PsiElement): Scope {
        synchronized(scope) {
            scopes.push(scope)
            if (topScope == null)
                topScope = scope
            psi.putUserData(scopeKey, scope)
            curScope?.add(scope)
            curScope = scope
        }
        return scope
    }

    private fun pop(): Scope {
        synchronized(scopes) {
            val pop = scopes.pop()
            curScope = if (scopes.isEmpty()) topScope else scopes.peek()
            return pop
        }
    }

    fun buildTree(file: PsiFile) {
        synchronized(scopes) {
            //val t = System.currentTimeMillis()
            scopes.clear()
            topScope = null
            curScope = null
            file.accept(this)
            //println("build tree : ${file.name}, ${System.currentTimeMillis() - t}")
        }
    }

    abstract fun findScope(psi: PsiElement): Scope?

    abstract fun getPosition(psi: PsiElement): Int

    override fun walkUp(pin: PsiElement, process: (declaration: LuaDeclarationTree.IDeclaration) -> Boolean) {
        assert(pin.containingFile == file)
        val scope = findScope(pin)
        scope?.walkUp(getPosition(pin), 0, process)
    }

    private fun createDeclaration(name: String, psi: PsiNamedElement, flags: Int): Declaration {
        val first = if (psi is LuaExpr) find(psi) else null
        return Declaration(name, getPosition(psi), psi, flags, first)
    }

    override fun find(expr: LuaExpr): Declaration? {
        if (expr is LuaIndexExpr || expr is LuaNameExpr) {
            val scope = findScope(expr)
            return scope?.find(expr)?.firstDeclaration
        }
        return null
    }

    override fun visitNameDef(o: LuaNameDef) {
        curScope?.add(createDeclaration(o.name, o, DeclarationFlag.Local))
    }

    override fun visitParamNameDef(o: LuaParamNameDef) {
        curScope?.add(createDeclaration(o.name, o, DeclarationFlag.Local))
    }

    override fun visitLocalFuncDef(o: LuaLocalFuncDef) {
        val name = o.name
        if (name != null)
            curScope?.add(createDeclaration(name, o, DeclarationFlag.Local or DeclarationFlag.Function))
        super.visitLocalFuncDef(o)
    }

    override fun visitClassMethodDef(o: LuaClassMethodDef) {
        val name = o.name
        if (name != null) {
            val parentExpr = o.classMethodName.expr
            find(parentExpr)?.addField(createDeclaration(name, o, DeclarationFlag.Function or DeclarationFlag.ClassMember))
        }
        super.visitClassMethodDef(o)
    }

    override fun visitClassMethodName(o: LuaClassMethodName) {
    }

    override fun visitAssignStat(o: LuaAssignStat) {
        o.varExprList.exprList.forEach { expr ->
            if (expr is LuaNameExpr) {
                val flags = find(expr)?.flags ?: DeclarationFlag.Global
                curScope?.add(createDeclaration(expr.name, expr, flags))
            } else if (expr is LuaIndexExpr) {
                val fieldName = expr.name ?: (expr.idExpr as? LuaLiteralExpr)?.let { "[${it.text}]" }

                if (fieldName != null) {
                    val declaration = curScope?.find(expr.prefixExpr)
                    declaration?.addField(createDeclaration(fieldName, expr, DeclarationFlag.ClassMember))
                }
            }
        }
        super.visitAssignStat(o)
    }

    protected open fun visitElementExt(element: PsiElement) {
        super.visitElement(element)
    }

    override fun visitElement(element: PsiElement) {
        if (element is LuaDeclarationScope) {
            push(element)
            visitElementExt(element)
            pop()
        } else visitElementExt(element)
    }
}

private class LuaDeclarationTreePsi(file: PsiFile) : LuaDeclarationTreeBase(file) {
    override fun findScope(psi: PsiElement): Scope? {
        var cur: PsiElement? = psi
        while (cur != null) {
            if (cur is LuaDeclarationScope) {
                var scope = cur.getUserData(scopeKey)
                if (scope == null) {
                    buildTree(psi.containingFile)
                    scope = cur.getUserData(scopeKey)
                }
                return scope
            }
            cur = cur.parent
        }
        return null
    }

    override fun getPosition(psi: PsiElement): Int {
        if (psi is PsiFile) return 0
        return psi.node.startOffset
    }
}

private class LuaDeclarationTreeStub(file: PsiFile) : LuaDeclarationTreeBase(file)  {
    val map = mutableMapOf<PsiElement, Int>()
    var count = 0

    override fun shouldRebuild(): Boolean {
        return super.shouldRebuild() || (file as? LuaPsiFile)?.isContentsLoaded == true
    }

    override fun visitElementExt(element: PsiElement) {
        var stub: STUB_ELE? = null
        if (element is LuaPsiFile) {
            stub = element.stub
        }
        if (element is STUB_PSI) {
            stub  = element.stub
        }
        if (stub != null) {
            for (child in stub.childrenStubs) {
                child.psi.accept(this)
            }
        } else super.visitElementExt(element)
    }

    override fun findScope(psi: PsiElement): Scope? {
        if (psi is STUB_PSI) {
            val stub = psi.stub
            if (stub != null) {
                var cur: STUB_ELE? = stub
                while (cur != null) {
                    val stubPsi = cur.psi
                    if (stubPsi is LuaDeclarationScope)  {
                        return stubPsi.getUserData(scopeKey)
                    }
                    cur = cur.parentStub
                }
            }
        }
        return null
    }

    override fun getPosition(psi: PsiElement): Int {
        return map.getOrPut(psi) { count++ }
    }
}
