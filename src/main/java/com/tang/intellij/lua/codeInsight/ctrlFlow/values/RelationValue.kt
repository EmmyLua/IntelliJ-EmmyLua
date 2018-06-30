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

import com.tang.intellij.lua.psi.LuaBinaryOp
import com.tang.intellij.lua.psi.LuaTypes

enum class RelationType(val description: String) {
    Unknown("Unknown"),
    LE("<="),
    LT("<"),
    GE(">="),
    GT(">"),
    EQ("=="),
    NE("~=");

    override fun toString(): String {
        return description
    }

    fun getNegatedType(): RelationType {
        return when (this) {
            RelationType.LE -> RelationType.GT
            RelationType.LT -> RelationType.GE
            RelationType.GE -> RelationType.LT
            RelationType.GT -> RelationType.LE
            RelationType.EQ -> RelationType.NE
            RelationType.NE -> RelationType.EQ
            else -> RelationType.Unknown
        }
    }

    companion object {
        fun from(op: LuaBinaryOp): RelationType {
            return when(op.node.firstChildNode.elementType) {
                LuaTypes.LE -> RelationType.LE
                LuaTypes.LT -> RelationType.LT
                LuaTypes.GE -> RelationType.GE
                LuaTypes.GT -> RelationType.GT
                LuaTypes.EQ -> RelationType.EQ
                LuaTypes.NE -> RelationType.NE
                else -> RelationType.Unknown
            }
        }
    }
}

class RelationValue(
        val left: VMValue,
        val right: VMValue,
        val relation: RelationType,
        factory: VMValueFactory
) : VMValueImpl(factory), ConditionValue {

    override fun createNegated(): ConditionValue {
        return factory.relationValueFactory.create(left, relation.getNegatedType(), right)
    }

    override fun toString(): String {
        return "$left $relation $right"
    }
}

class RelationValueFactory(val factory: VMValueFactory) {
    private val myValues = mutableMapOf<Int, RelationValue>()

    fun create(left: VMValue, relation: RelationType, right: VMValue): RelationValue {
        if (left.id > right.id)
            return create(right, relation, left)
        val hash = BinaryOpValueFactory.calcHash(left, right, relation)
        return myValues.getOrPut(hash) { RelationValue(left, right, relation, factory) }
    }
}