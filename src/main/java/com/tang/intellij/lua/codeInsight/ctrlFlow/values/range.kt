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

import java.lang.Long.max
import java.lang.Long.min

abstract class Range {
    companion object {
        val EMPTY: Range = Empty()
        val ALL = RangeMinMax(Long.MIN_VALUE, Long.MAX_VALUE)

        fun range(min: Long, max: Long): Range {
            return if (min == max) Point(min) else RangeMinMax(min, max)
        }
    }

    abstract val min: Long
    abstract val max: Long

    override fun toString(): String {
        return "Range()"
    }

    fun fromRelation(relationType: RelationType): Range {
        if (this.isEmpty)
            return this

        return when (relationType) {
            RelationType.EQ -> this
            RelationType.NE -> ALL.subtract(this)
            // <
            RelationType.LT -> RangeMinMax(Long.MIN_VALUE, max - 1)
            // <=
            RelationType.LE -> RangeMinMax(Long.MIN_VALUE, max)
            // >
            RelationType.GT -> RangeMinMax(min + 1, Long.MAX_VALUE)
            // >=
            RelationType.GE -> RangeMinMax(min, Long.MAX_VALUE)
            //TODO fromRelation
            else -> this
        }
    }

    abstract fun intersect(other: Range): Range

    abstract fun subtract(other: Range): Range

    abstract fun contains(p: Point): Boolean

    open val isEmpty: Boolean get() = this == EMPTY
}

class Point(val value:Long) : Range() {
    override val min: Long
        get() = value
    override val max: Long
        get() = value

    override fun contains(p: Point): Boolean {
        return p.value == value
    }

    override fun subtract(other: Range): Range {
        return if (other.contains(this)) EMPTY else this
    }

    override fun intersect(other: Range): Range {
        return if (other.contains(this)) this else EMPTY
    }

    override fun toString(): String {
        return "Point($value)"
    }
}

private class Empty : Range() {
    override val min: Long
        get() = TODO()
    override val max: Long
        get() = TODO()

    override fun subtract(other: Range): Range {
        return this
    }

    override fun intersect(other: Range): Range {
        return this
    }

    override fun contains(p: Point): Boolean = false

    override fun toString(): String {
        return "Empty()"
    }
}

class RangeMinMax(override val min: Long, override val max: Long) : Range() {
    override fun subtract(other: Range): Range {
        if (other == this) return EMPTY
        if (other.isEmpty) return this

        if (other is Point) {
            return when {
                min > other.value || max < other.value -> this
                min == other.value -> RangeMinMax(min + 1, max)
                max == other.value -> RangeMinMax(min, max - 1)
                else -> RangeSet(arrayOf(min, other.value - 1, other.value + 1, max))
            }
        }
        if (other is RangeMinMax) {
            return when {
                other.min > max || other.max < min -> this
                other.min <= min && other.max >= max -> EMPTY
                other.min > min && other.max < max -> RangeSet(arrayOf(min, other.min - 1, other.max + 1, max))
                other.min <= min -> RangeMinMax(other.min + 1, max)
                else -> RangeMinMax(min, other.max - 1)
            }
        }

        var ret: Range = this
        if (other is RangeSet) {
            for (i in 0 until other.length) {
                val range = other.get(i)
                ret = ret.subtract(range)
                if (ret.isEmpty) return ret
            }
        }
        return ret
    }

    override fun intersect(other: Range): Range {
        if (other == this) return this
        if (other.isEmpty) return other
        if (other is Point) return other.intersect(this)
        if (other is RangeMinMax) {
            return when {
                other.min <= min && other.max >= max -> this
                other.min >= min && other.max <= max -> other
                else -> {
                    val min = max(other.min, this.min)
                    val max = min(other.max, this.max)
                    if (min > max)
                        EMPTY
                    else
                        range(min, max)
                }
            }
        }
        return EMPTY
    }

    override fun contains(p: Point): Boolean {
        return p.value in min..max
    }

    override fun toString(): String {
        return "RangeMinMax($min, $max)"
    }
}

class RangeSet : Range {

    val list: Array<RangeMinMax>

    val length: Int get() = list.size

    constructor(list: Array<RangeMinMax>) {
        assert(list.isNotEmpty())
        this.list = list
    }

    constructor(array: Array<Long>) {
        assert(array.size % 2 == 0)
        assert(array.isNotEmpty())

        val list = mutableListOf<RangeMinMax>()
        for (i in 0 until array.size step 2) {
            list.add(RangeMinMax(array[i], array[i + 1]))
        }
        this.list = list.toTypedArray()
    }

    override val min: Long
        get() = list.first().min

    override val max: Long
        get() = list.last().max

    fun get(i: Int): RangeMinMax {
        return list[i]
    }

    override fun subtract(other: Range): Range {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun intersect(other: Range): Range {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun contains(p: Point): Boolean {
        return list.any { it.contains(p) }
    }

    override fun toString(): String {
        return buildString {
            append("RangeSet(")
            list.joinToString(", ")
            append(")")
        }
    }
}