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

import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.psi.LuaBinaryOp
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.psi.LuaUnaryOp

class BinaryOpValue(
        val l: VMValue,
        val r: VMValue,
        val relation: RelationType,
        factory: VMValueFactory
) : VMValueImpl(factory) {
    override fun toString(): String {
        return "$l $relation $r"
    }
}

enum class BinaryOp(val description: String, val eType: IElementType) {
    AND("and", LuaTypes.AND),
    OR("or", LuaTypes.OR),

    PLUS("+", LuaTypes.PLUS),
    MINUS("-", LuaTypes.MINUS),
    MULT("*", LuaTypes.MULT),
    DIV("/", LuaTypes.DIV),
    MOD("%", LuaTypes.MOD),
    BIT_LTLT("<<", LuaTypes.BIT_LTLT),
    BIT_GTGT(">>", LuaTypes.BIT_RTRT),
    BIT_OR("|", LuaTypes.BIT_OR),
    BIT_AND("&", LuaTypes.BIT_AND);

    override fun toString(): String {
        return description
    }

    companion object {
        fun from(op: LuaBinaryOp): BinaryOp? {
            val eType = op.node.firstChildNode.elementType
            return BinaryOp.values().find { it.eType == eType }
        }
    }
}

enum class UnaryOp(val description: String, val eType: IElementType) {
    MINUS("-", LuaTypes.MINUS),
    NOT("not", LuaTypes.NOT),
    GETN("#", LuaTypes.GETN),
    BIT_TILDE("~", LuaTypes.BIT_TILDE);

    override fun toString(): String {
        return description
    }

    companion object {
        fun from(op: LuaUnaryOp): UnaryOp? {
            val eType = op.node.firstChildNode.elementType
            return values().find { it.eType == eType }
        }
    }
}

class BinaryOpValueFactory(private val factory: VMValueFactory) {
    private val binOpValues = mutableMapOf<Int, BinaryOpValue>()

    companion object {
        fun calcHash(l: VMValue, r: VMValue, op: RelationType): Int {
            var result = l.hashCode()
            result = 31 * result + r.hashCode()
            result = 31 * result + op.ordinal
            return result
        }
    }

    fun create(l: VMValue, r: VMValue, relation: RelationType): VMValue {
        if (l.id > r.id)
            return create(r, l, relation)

        return when (relation) {
            RelationType.LE,
            RelationType.LT,
            RelationType.GE,
            RelationType.GT,
            RelationType.NE,
            RelationType.EQ -> factory.relationValueFactory.create(l, relation, r)
            else -> {
                val hash = calcHash(l, r, relation)
                binOpValues.getOrPut(hash) { BinaryOpValue(l, r, relation, factory) }
            }
        }
    }
}