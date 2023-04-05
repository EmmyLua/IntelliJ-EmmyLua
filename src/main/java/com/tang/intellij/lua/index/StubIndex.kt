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

package com.tang.intellij.lua.index

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.indexing.IndexId
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.psi.LuaPsiElement

interface IndexSink {
    fun indexClassMember(clazz: String, name: String, psi: LuaClassMember)
    fun indexShortName(name: String, psi: LuaPsiElement)
    fun <Psi : PsiElement, K> occurrence(indexKey: IndexId<K, Psi>, key: K, value: Psi)
}

object StubKeys {
    val CLASS_MEMBER: IndexId<Int, LuaClassMember> = IndexId.create("lua.index.class.member")
    val SHORT_NAME: IndexId<String, LuaPsiElement> = IndexId.create("lua.index.short_name")

    fun removeStubs(fileId: Int) {
        ClassMemberIndex.instance.removeStubs(fileId)
    }
}

abstract class StubIndex<K, Psi : PsiElement> {
    inner class StubFile {
        val elements = mutableListOf<Psi>()
    }
    inner class StubEntry(val key: K) {
        val files = mutableMapOf<Int, StubFile>()
    }

    abstract fun getKey(): IndexId<K, Psi>

    private var lock = false

    private val indexMap = mutableMapOf<K, StubEntry>()

    @Synchronized
    fun get(key: K, project: Project, scope: GlobalSearchScope): MutableList<Psi> {
        val list = mutableListOf<Psi>()
        if (lock)
            return list
        val stubEntry = indexMap[key]
        stubEntry?.files?.forEach { (_, u) -> list.addAll(u.elements) }

        return list
    }

    @Synchronized
    fun processKeys(project: Project, scope: GlobalSearchScope, processor: Processor<K>): Boolean {
        if (lock)
            return true
        return ContainerUtil.process(indexMap.keys, processor)
    }

    @Synchronized
    fun processValues(project: Project, scope: GlobalSearchScope, processor: Processor<Psi>) {
        for (stubEntry in indexMap.values) {
            for (stubFile in stubEntry.files.values) {
                if (!ContainerUtil.process(stubFile.elements, processor))
                    return
            }
        }
    }

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <Psi1 : PsiElement, K1> occurrence(fileId: Int, key: K1, value: Psi1) {
        val k = key as K
        val stubEntry = indexMap.getOrPut(k) { StubEntry(k) }
        val stubFile = stubEntry.files.getOrPut(fileId) { StubFile() }
        stubFile.elements.add(value as Psi)
    }

    @Synchronized
    fun removeStubs(fileId: Int) {
        val iterator = indexMap.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.files.remove(fileId)
            if (entry.value.files.isEmpty())
                iterator.remove()
        }
    }
}