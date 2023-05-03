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

        fun infer(psi: LuaTypeGuessable, context: SearchContext): ITy {
            return with(context, Ty.UNKNOWN) { it.inferAndCache(psi) }
        }

        private fun <T> with(ctx: SearchContext, defaultValue: T, action: (ctx: SearchContext) -> T): T {
            return if (ctx.myInStack) {
                action(ctx)
            } else {
                val stack = threadLocal.get()
                val size = stack.size
                stack.push(ctx)
                ctx.myInStack = true
                val result = try {
                    action(ctx)
                } catch (e: Exception) {
                    defaultValue
                }
                ctx.myInStack = false
                stack.pop()
                assert(size == stack.size)
                result
            }
        }

        private fun <T> with(project: Project, action: (ctx: SearchContext) -> T): T {
            val ctx = get(project)
            return with(ctx, action)
        }

        @Suppress("UNUSED_PARAMETER")
        fun <T> withStub(project: Project, file: PsiFile, defaultValue: T, action: (ctx: SearchContext) -> T): T {
            val context = SearchContext(project)
            return withStub(context, defaultValue, action)
        }

        private fun <T> withStub(ctx: SearchContext, defaultValue: T, action: (ctx: SearchContext) -> T): T {
            return with(ctx, defaultValue) {
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

        fun invalidateCache(project: Project) {
            var searchContext = get(project)
            searchContext.invalidateInferCache()
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
    private var myScope: GlobalSearchScope? = null

    fun <T> withIndex(index: Int, action: () -> T): T {
        val savedIndex = this.index
        myIndex = index
        val ret = action()
        myIndex = savedIndex
        return ret
    }

    fun guessTuple() = index < 0

    val scope get(): GlobalSearchScope {
        if (isDumb)
            return GlobalSearchScope.EMPTY_SCOPE
        if (myScope == null) {
            myScope = ProjectAndLibrariesScope(project)
        }
        return myScope!!
    }

    val isDumb: Boolean
        get() = myDumb || DumbService.isDumb(project)

    val forStub get() = myForStub

    fun <T> withScope(scope: GlobalSearchScope, action: () -> T): T {
        val oriScope = myScope
        myScope = scope
        val ret = action()
        myScope = oriScope
        return ret
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

    private fun inferAndCache(psi: LuaTypeGuessable): ITy {
        /*if (inferCache.containsKey(psi)) {
            println("use cache!!!")
        }*/
        return myInferCache.getOrPut(psi) {
            ILuaTypeInfer.infer(psi, this)
        }
    }

    fun getTypeFromCache(psi: LuaTypeGuessable): ITy {
        return myInferCache.getOrElse(psi) { Ty.UNKNOWN }
    }

    fun invalidateInferCache() {
        myInferCache.clear()
    }
}
