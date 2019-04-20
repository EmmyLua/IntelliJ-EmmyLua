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

package com.tang.intellij.lua.search

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectAndLibrariesScope
import com.tang.intellij.lua.ext.ILuaTypeInfer
import com.tang.intellij.lua.psi.LuaTypeGuessable
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty
import java.util.*

/**

 * Created by tangzx on 2017/1/14.
 */
class SearchContext private constructor(val project: Project) {

    companion object {
        private val threadLocal = object : ThreadLocal<Stack<SearchContext>>() {
            override fun initialValue(): Stack<SearchContext> {
                return Stack()
            }
        }

        fun get(project: Project): SearchContext {
            val stack = threadLocal.get()
            return if (stack.isEmpty()) {
                SearchContext(project)
            } else {
                stack.peek()
            }
        }

        fun infer(psi: LuaTypeGuessable): ITy {
            return with(psi.project) { it.inferAndCache(psi) }
        }

        fun <T> with(ctx: SearchContext, action: (ctx: SearchContext) -> T): T {
            return if (ctx.myInStack) {
                action(ctx)
            } else {
                val stack = threadLocal.get()
                val size = stack.size
                stack.push(ctx)
                ctx.myInStack = true
                val result = action(ctx)
                ctx.myInStack = false
                stack.pop()
                assert(size == stack.size)
                result
            }
        }

        fun <T> with(project: Project, action: (ctx: SearchContext) -> T): T {
            val ctx = get(project)
            return with(ctx, action)
        }

        fun <T> withStub(project: Project, file: PsiFile, action: (ctx: SearchContext) -> T): T {
            return with(project) {
                val dumb = it.myDumb
                val stub = it.myForStub
                it.myDumb = true
                it.myForStub = true
                val ret = action(it)
                it.myDumb = dumb
                it.myForStub = stub
                ret
            }
        }
    }

    /**
     * 用于有多返回值的索引设定
     */
    val index: Int get() = myIndex

    private var myDumb = false
    private var myForStub = false
    private var myIndex = -1
    private var myInStack = false
    private val myGuardList = mutableListOf<InferRecursionGuard>()
    private val myInferCache = mutableMapOf<LuaTypeGuessable, ITy>()

    fun <T> withIndex(index: Int, action: () -> T): T {
        val savedIndex = this.index
        myIndex = index
        val ret = action()
        myIndex = savedIndex
        return ret
    }

    fun guessTuple() = index < 0

    private var scope: GlobalSearchScope? = null

    fun getScope(): GlobalSearchScope {
        if (scope == null) {
            scope = if (isDumb) {
                GlobalSearchScope.EMPTY_SCOPE
            } else {
                ProjectAndLibrariesScope(project)
            }
        }
        return scope!!
    }

    val isDumb: Boolean
        get() = myDumb || DumbService.isDumb(project)

    val forStub get() = myForStub

    fun clone(): SearchContext {
        val c = SearchContext(project)
        c.myDumb = myDumb
        c.myForStub = myForStub
        return c
    }

    fun withRecursionGuard(psi: PsiElement, type: GuardType, action: () -> ITy): ITy {
        myGuardList.forEach {
            if (it.check(psi, type)) {
                return Ty.UNKNOWN
            }
        }
        val guard = createGuard(psi, type)
        if (guard != null)
            myGuardList.add(guard)
        val result = action()
        if (guard != null)
            myGuardList.remove(guard)
        return result
    }

    private fun inferAndCache(psi: LuaTypeGuessable):  ITy {
        /*if (inferCache.containsKey(psi)) {
            println("use cache!!!")
        }*/
        return myInferCache.getOrPut(psi) {
            ILuaTypeInfer.infer(psi, this)
        }
    }
}