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

package com.tang.intellij.lua.codeInsight.ctrlFlow.values

@Suppress("UNCHECKED_CAST")
class FactMap {
    private var map:Map<FactType<*>, Any> = mutableMapOf()

    constructor()

    constructor(map: Map<FactType<*>, Any>) {
        this.map = map
    }

    fun <T> with(type: FactType<T>, value: T?): FactMap {
        return if (value == null || type.isUnknown(value)) minus(type) else plus(type, value)
    }

    private fun <T> minus(type: FactType<T>): FactMap {
        return FactMap(map.minus(type))
    }

    private fun <T> plus(type: FactType<T>, value: T): FactMap {
        return FactMap(map.plus(Pair(type, value as Any)))
    }

    fun <T> getFact(factType: FactType<T>): T? {
        return map[factType] as T?
    }

    fun <T> intersect(factType: FactType<T>, value: T?): FactMap? {
        if (value == null || factType.isUnknown(value))
            return this
        val cur = getFact(factType) ?: return with(factType, value)
        val new = factType.intersectFacts(cur, value)
        return if (new == null) null else with(factType, new)
    }

    override fun toString(): String {
        return buildString {
            map.forEach { t, u ->
                append("[$t -> $u]")
            }
        }
    }

    companion object {
        val EMPTY = FactMap()

        fun fromValue(value: VMValue): FactMap {
            var map = EMPTY
            FactType.all.forEach { type ->
                map = map.with(type as FactType<Any>, type.fromValue(value))
            }
            return map
        }
    }
}

class FactMapValue(
        val facts: FactMap,
        factory: VMValueFactory
) : VMValueImpl(factory) {
    fun <T> with(type: FactType<T>, value: T): VMValue {
        return factory.factMapValueFactory.create(facts.with(type, value))
    }
}

class FactMapValueFactory(val factory: VMValueFactory) {
    fun create(facts: FactMap): VMValue {
        return FactMapValue(facts, factory)
    }

    fun <T> create(type: FactType<T>, value: T): VMValue {
        return FactMapValue(FactMap.EMPTY.with(type, value), factory)
    }
}