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

package com.tang.intellij.lua.codeInsight.ctrlFlow

import com.tang.intellij.lua.codeInsight.ctrlFlow.values.ConstantValue
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.VMValue
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.VMValueFactory
import com.tang.intellij.lua.codeInsight.ctrlFlow.values.VariableValue

class ValueGroup {
    val factory: VMValueFactory
    val index: Int

    private val array = mutableListOf<Int>()

    constructor(factory: VMValueFactory, index: Int){
        this.factory = factory
        this.index = index
    }

    constructor(toCopy: ValueGroup) : this(toCopy.factory, toCopy.index) {
        array.addAll(toCopy.array)
    }

    val size get() = array.size

    fun get(index: Int): Int {
        return array[index]
    }

    fun add(valueId: Int) {
        array.add(valueId)
    }

    fun remove(valueId: Int) {
        array.remove(valueId)
    }

    fun each(action: (value: VMValue) -> Unit) {
        for (i in array) {
            val v = factory.get(i)
            if (v != null)
                action(v)
        }
    }

    fun <T> each(clazz: Class<T>, action: (value: T) -> Unit) {
        for (i in array) {
            val v = factory.get(i)
            if (clazz.isInstance(v)) {
                action(clazz.cast(v))
            }
        }
    }

    fun findConstant(): ConstantValue? {
        for (i in array) {
            val v = factory.get(i)
            if (v is ConstantValue) return v
        }
        return null
    }

    override fun toString(): String {
        return buildString {
            append("(")
            append(array.joinToString(separator = ", ") {
                val v = factory.get(it)
                v?.toString() ?: "null"
            })
            append(")")
        }
    }
}

class ValueGroupManager(val factory: VMValueFactory) {
    private val valueId2GroupIndex = mutableMapOf<Int, Int?>()

    private val groups = mutableListOf<ValueGroup?>()

    constructor(toCopy: ValueGroupManager): this(toCopy.factory) {
        valueId2GroupIndex.putAll(toCopy.valueId2GroupIndex)
        for (group in toCopy.groups) {
            if (group != null) {
                groups.add(ValueGroup(group))
            } else {
                groups.add(null)
            }
        }
    }

    fun flushVariable(variable: VariableValue) {
        val group = get(variable)
        if (group != null) {
            group.remove(variable.id)
            if (group.size == 0) {
                groups[group.index] = null
            }
        }
        valueId2GroupIndex.remove(variable.id)
    }

    fun unite(v1: VMValue, v2: VMValue): Boolean {
        if (v1 == v2)
            return true
        if (!canBeRelation(v1) || !canBeRelation(v2))
            return true
        val g1 = getOrCreate(v1)
        val g2 = getOrCreate(v2)
        if (g1.index == g2.index)
            return true
        if (g1.findConstant() != null && g2.findConstant() != null)
            return false

        // merger
        for (i in 0 until g2.size) {
            val valueId = g2.get(i)
            g1.add(valueId)
            valueId2GroupIndex[valueId] = g1.index
        }
        groups[g2.index] = null
        return true
    }

    private fun getNextGroupIndex(): Int {
        var groupIndex = groups.indexOf(null)
        if (groupIndex == -1) {
            groupIndex = groups.size
            groups.add(null)
        }
        return groupIndex
    }

    fun get(v: VMValue): ValueGroup? {
        val gIndex = valueId2GroupIndex[v.id]
        return if (gIndex == null) null else groups[gIndex]
    }

    fun getConstantValue(v: VMValue): ConstantValue? {
        if (v is ConstantValue)
            return v
        return get(v)?.findConstant()
    }

    private fun getOrCreate(v: VMValue): ValueGroup {
        var group = get(v)
        if (group == null) {
            group = ValueGroup(factory, getNextGroupIndex())
            group.add(v.id)
            groups[group.index] = group
            valueId2GroupIndex[v.id] = group.index
        }
        return group
    }

    fun canBeRelation(value: VMValue): Boolean {
        return value is VariableValue|| value is ConstantValue
    }

    override fun toString(): String {
        return buildString {
            append("[")
            append(groups.joinToString(separator = ", "))
            append("]")
        }
    }
}