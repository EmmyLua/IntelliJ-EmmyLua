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

import com.tang.intellij.lua.codeInsight.ctrlFlow.values.*
import java.util.*

open class VMStateImpl : VMState {

    private val stack = Stack<VMValue>()
    private val variableStates = mutableMapOf<VariableValue, ValueState>()
    private val eqManager: ValueGroupManager
    private val factory: VMValueFactory

    override val id = idIndex++

    companion object {
        private var idIndex = 0
    }

    constructor(factory: VMValueFactory) {
        this.factory = factory
        eqManager = ValueGroupManager(factory)
    }

    constructor(toCopy: VMStateImpl) {
        factory = toCopy.factory
        eqManager = ValueGroupManager(toCopy.eqManager)
        stack.addAll(toCopy.stack)
        variableStates.putAll(toCopy.variableStates)
    }

    override fun isNil(value: VMValue): Boolean {
        val const = eqManager.getConstantValue(value)
        if (const is NilValue) {
            return true
        }
        if (value is VariableValue) {
            val state = getValueState(value)
            if (state.isNil)
                return true
        }
        return false
    }

    override fun isNotNil(value: VMValue): Boolean {
        val const = eqManager.getConstantValue(value)
        if (const is NilValue) {
            return false
        }
        if (value is VariableValue) {
            val state = getValueState(value)
            if (state.isNotNil)
                return true
        }
        return false
    }

    override fun push(value: VMValue) {
        stack.push(value)
    }

    override fun pop(): VMValue {
        return stack.pop()
    }

    override fun peek(): VMValue = stack.peek()

    override fun setVariableValue(variable: VariableValue, value: VMValue) {
        if (variable == value)
            return
        flushVariable(variable)

        var state = getValueState(variable)
        state = when (value) {
            is NilValue -> state.withNil()
            is VariableValue -> {
                val targetState = getValueState(value)
                state.withFacts(targetState.facts)
            }
            is NumberValue -> {
                state.withNotNil().withFact(FactType.RANGE, FactType.RANGE.fromValue(value))
            }
            is FactMapValue -> state.withFacts(value.facts)
            is UnknownValue -> state.withoutFact(FactType.NULLABILITY)
            else -> state.withNotNil()
        }
        setValueState(variable, state)
        applyRelation(variable, value)
    }

    private fun <T> getValueFact(type: FactType<T>, value: VMValue): T? {
        if (value is VariableValue) {
            return getValueState(value).getFact(type)
        }
        return type.fromValue(value)
    }

    override fun flushVariable(variable: VariableValue) {
        eqManager.flushVariable(variable)
    }

    override fun createCopy(): VMState {
        return VMStateImpl(this)
    }

    override fun createClosure(): VMState {
        TODO()
    }

    override fun castCondition(value: VMValue): ConditionValue {
        return when (value) {
            // RelationValue && BooleanValue
            is ConditionValue -> value
            is VariableValue -> {
                val constantValue = eqManager.getConstantValue(value)
                if (constantValue != null)
                    return castCondition(constantValue)
                // a ~= nil
                return factory.relationValueFactory.create(value, RelationType.NE, factory.constantValueFactory.NIL)
            }
            is StringValue, is NumberValue -> factory.constantValueFactory.TRUE
            is NilValue -> factory.constantValueFactory.FALSE
            else -> factory.constantValueFactory.TRUE
        }
    }

    override fun getValueState(variable: VariableValue): ValueState {
        return variableStates.getOrPut(variable) { VariableValueState(variable) }
    }

    private fun setValueState(variable: VariableValue, state: ValueState) {
        variableStates[variable] = state
    }

    override fun <T> setVariableFact(variable: VariableValue, factType: FactType<T>, value: T?) {
        val state = getValueState(variable)
        val newState = state.withFact(factType, value)
        setValueState(variable, newState)
    }

    override fun applyCondition(cond: ConditionValue): Boolean {
        if (cond is RelationValue) {
            return applyRelationCondition(cond.left, cond.right, cond.relation)
        }
        if (cond is BooleanValue) {
            return cond.value
        }
        return true
    }

    private fun applyRelationCondition(l: VMValue, r: VMValue, type: RelationType): Boolean {
        // range
        val rangeL = getValueFact(FactType.RANGE, l)
        val rangeR = getValueFact(FactType.RANGE, r)
        if (rangeL != null) {
            if (!applyFact(r, FactType.RANGE, rangeL.fromRelation(type)))
                return false
        }
        if (rangeR != null) {
            if (!applyFact(l, FactType.RANGE, rangeR.fromRelation(type)))
                return false
        }

        // relation
        val isNegated = type != RelationType.EQ
        return applyUnboxedRelation(l, r, isNegated)
    }

    private fun applyUnboxedRelation(l: VMValue, r: VMValue, isNegated: Boolean = false): Boolean {
        // (a == a) => true, (a != a) => false
        if (l == r)
            return !isNegated
        if ((isNotNil(l) && isNil(r)) || (isNil(l) && isNotNil(r)))
            return isNegated

        val lg = eqManager.get(l)
        val rg = eqManager.get(r)
        // local b = a; (b == a) => true
        if (lg != null && lg == rg)
            return !isNegated

        // constant eq
        val constL = l as? ConstantValue ?: lg?.findConstant()
        val constR = r as? ConstantValue ?: rg?.findConstant()
        if (constL != null && constR != null) {
            return constL == constR != isNegated
        }

        return applyRelation(l, r, isNegated)
    }

    private fun applyRelation(l: VMValue, r: VMValue, isNegated: Boolean = false): Boolean {
        // can't compare
        if (!eqManager.canBeRelation(l) || !eqManager.canBeRelation(r))
            return true

        if (isNegated) { // ~=
            if (!applyInequality(l, r) && !applyInequality(r, l))
                return false
        } else { // ==
            if (!eqManager.unite(l, r))
                return false
        }

        return true
    }

    private fun applyInequality(l: VMValue, r: VMValue): Boolean {
        if (r !is ConstantValue) return false
        if (l !is VariableValue) return false
        if (r is NilValue) { // a ~= nil
            if (isNil(l))
                return false
            val state = getValueState(l)
            if (state.isNil)
                return false
            setValueState(l, state.withNotNil())
            return true
        }
        // todo: number
        if (r !is BooleanValue)
            return true
        return applyRelation(l, factory.getBool(!r.value))
    }

    private fun <T> applyFact(value: VMValue, type: FactType<T>, fact: T): Boolean {
        if (value is VariableValue) {
            val state = getValueState(value)
            var newState = state.intersect(type, fact) ?: return false
            // not nil
            if (type == FactType.RANGE)
                newState = newState.withFact(FactType.NULLABILITY, Nullability.NOT_NULL)
            setValueState(value, newState)
        }
        return true
    }

    override fun hashCode(): Int {
        var result = stack.hashCode()
        result = 31 * result + variableStates.hashCode()
        result = 31 * result + eqManager.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VMStateImpl) return false

        if (stack != other.stack) return false
        if (eqManager != other.eqManager) return false
        if (variableStates != other.variableStates) return false

        return true
    }
}