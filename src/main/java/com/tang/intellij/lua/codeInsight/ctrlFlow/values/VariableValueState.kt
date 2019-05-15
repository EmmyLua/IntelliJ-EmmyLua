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

class VariableValueState : ValueState {

    private var myValue: VMValue? = null

    private val myFactMap: FactMap

    override val facts: FactMap
        get() = myFactMap

    constructor(factMap: FactMap, value: VMValue? = null) {
        this.myFactMap = factMap
        this.myValue = value
    }

    constructor(variable: VariableValue) {
        this.myFactMap = FactMap.EMPTY
    }

    override fun withNil(): ValueState {
        return if (isNil) this else withFact(FactType.NULLABILITY, Nullability.NULL)
    }

    override val isNil: Boolean
        get() = getFact(FactType.NULLABILITY) == Nullability.NULL

    override fun withNotNil(): ValueState {
        return if (isNotNil) this else withFact(FactType.NULLABILITY, Nullability.NOT_NULL)
    }

    override val isNotNil: Boolean
        get() = getFact(FactType.NULLABILITY) == Nullability.NOT_NULL

    override fun <T> withFact(factType: FactType<T>, value: T?): ValueState {
        return VariableValueState(myFactMap.with(factType, value))
    }

    override fun <T> withoutFact(factType: FactType<T>): ValueState {
        return withFact(factType, null)
    }

    override fun <T> getFact(factType: FactType<T>): T? {
        return myFactMap.getFact(factType)
    }

    override fun toString(): String {
        return myFactMap.toString()
    }

    override fun withFacts(facts: FactMap?): ValueState {
        return if (facts == null) this else VariableValueState(facts)
    }

    override fun withValue(value: VMValue): ValueState {
        return if (value == myValue) this else VariableValueState(myFactMap, value)
    }

    override fun <T> intersect(factType: FactType<T>, value: T?): ValueState? {
        val map = myFactMap.intersect(factType, value)
        return if (map == null) null else withFacts(map)
    }

    override fun hashCode(): Int {
        return myFactMap.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VariableValueState) return false

        if (myFactMap != other.myFactMap) return false

        return true
    }
}