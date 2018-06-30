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

import com.tang.intellij.lua.psi.LuaExpr
import com.tang.intellij.lua.psi.LuaLiteralExpr
import com.tang.intellij.lua.psi.LuaPsiElement

interface VMValue {
    val id: Int
}

@Suppress("LeakingThis")
abstract class VMValueImpl(protected val factory: VMValueFactory) : VMValue {
    override val id = factory.register(this)

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        return other is VMValue && other.id == id
    }

    open fun castToBool(): BooleanValue {
        return factory.constantValueFactory.TRUE
    }
}

interface ConditionValue : VMValue {
    fun createNegated(): ConditionValue
}

interface ValueState {
    fun <T> withFact(factType: FactType<T>, value: T?): ValueState

    fun withNil(): ValueState

    val isNil: Boolean

    fun withNotNil(): ValueState

    val isNotNil: Boolean

    fun <T> withoutFact(factType: FactType<T>): ValueState

    fun <T> getFact(factType: FactType<T>): T?

    val facts: FactMap

    fun withFacts(facts: FactMap?): ValueState

    fun withValue(value: VMValue): ValueState

    fun <T> intersect(factType: FactType<T>, value: T?): ValueState?
}

interface VMValueFactory {
    val scopeDepth: Int

    val factMapValueFactory: FactMapValueFactory

    val binaryOpValueFactory: BinaryOpValueFactory

    val constantValueFactory: ConstantValueFactory

    val relationValueFactory : RelationValueFactory

    fun createValue(expr: LuaExpr): VMValue

    fun createVariableValue(name: String, psi: LuaPsiElement): VariableValue

    fun createLiteralValue(expr: LuaLiteralExpr): VMValue

    fun register(value: VMValue): Int

    fun get(id: Int): VMValue?

    fun getBool(value: Boolean): BooleanValue

    fun findVariable(name: String): VariableValue?
}

class UnknownValue(factory: VMValueFactory) : VMValueImpl(factory) {
    override fun toString(): String {
        return "Unknown"
    }
}

abstract class ConstantValue(factory: VMValueFactory) : VMValueImpl(factory)

class NilValue(factory: VMValueFactory) : ConstantValue(factory) {

    override fun castToBool(): BooleanValue {
        return factory.constantValueFactory.FALSE
    }

    override fun toString(): String {
        return "nil"
    }
}

class BooleanValue(val value: Boolean, factory: VMValueFactory) : ConstantValue(factory), ConditionValue {
    override fun toString(): String {
        return "bool: $value"
    }

    override fun castToBool(): BooleanValue {
        return this
    }

    override fun createNegated(): ConditionValue {
        return factory.getBool(!value)
    }
}

class StringValue(val value: String, factory: VMValueFactory) : ConstantValue(factory) {
    override fun toString(): String {
        return value
    }

    override fun equals(other: Any?): Boolean {
        return other is StringValue && other.value == value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

class NumberValue(val value: Float, factory: VMValueFactory) : ConstantValue(factory) {
    override fun toString(): String {
        return "number: $value"
    }

    override fun equals(other: Any?): Boolean {
        return other is NumberValue && other.value == value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

class VariableValue(val name: String, factory: VMValueFactory) : VMValueImpl(factory) {

    override fun toString(): String {
        return name
    }
}