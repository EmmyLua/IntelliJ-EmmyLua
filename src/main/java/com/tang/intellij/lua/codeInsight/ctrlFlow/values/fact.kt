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

abstract class FactType<T> {
    companion object {
        val NULLABILITY = NullabilityFactType()
        val RANGE = RangeFactType()
        val LUA_TYPE = LuaTypeFactType()

        val all = arrayOf(
                NULLABILITY,
                RANGE,
                LUA_TYPE
        )
    }

    open fun fromValue(value: VMValue): T? {
        return if (value is FactMapValue) value.facts.getFact(this) else null
    }

    open fun intersectFacts(l: T, r: T): T? {
        return if (l == r) l else null
    }

    open fun isUnknown(value: T): Boolean {
        return false
    }
}

enum class Nullability {
    NULL, NOT_NULL
}

class NullabilityFactType : FactType<Nullability>() {
    override fun fromValue(value: VMValue): Nullability? {
        return when (value) {
            is NilValue -> Nullability.NULL
            is ConstantValue -> Nullability.NOT_NULL
            else -> super.fromValue(value)
        }
    }

    override fun intersectFacts(l: Nullability, r: Nullability): Nullability? {
        if (l == Nullability.NOT_NULL && r == Nullability.NOT_NULL)
            return Nullability.NOT_NULL
        return super.intersectFacts(l, r)
    }

    override fun toString(): String {
        return "Nullability"
    }
}

class RangeFactType : FactType<Range>() {
    override fun fromValue(value: VMValue): Range? {
        if (value is NumberValue) {
            // todo handle float
            return Point(value.value.toLong())
        }
        return super.fromValue(value)
    }

    override fun intersectFacts(l: Range, r: Range): Range? {
        val new = l.intersect(r)
        return if (new.isEmpty) null else new
    }

    override fun toString(): String {
        return "Range"
    }
}

enum class LuaType {
    NUMBER, STRING, NIL, TABLE, OTHER
}

class LuaTypeFactType : FactType<LuaType>() {
    override fun fromValue(value: VMValue): LuaType {
        return when (value) {
            is NumberValue -> LuaType.NUMBER
            is StringValue -> LuaType.STRING
            is NilValue -> LuaType.NIL
            // todo table type?
            else -> LuaType.OTHER
        }
    }

    override fun toString(): String {
        return "LuaType"
    }
}