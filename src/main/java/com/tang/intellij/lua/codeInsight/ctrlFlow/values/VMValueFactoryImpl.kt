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

import com.tang.intellij.lua.psi.*

@Suppress("LeakingThis")
open class VMValueFactoryImpl(val parent: VMValueFactory? = null) : VMValueFactory {
    private val values = mutableListOf<VMValue>()
    private val namedValues = mutableMapOf<String, VariableValue>()

    override val factMapValueFactory = FactMapValueFactory(this)
    override val binaryOpValueFactory = BinaryOpValueFactory(this)
    override val constantValueFactory = ConstantValueFactory(this)
    override val relationValueFactory = RelationValueFactory(this)

    override val scopeDepth: Int
        get() = if (parent == null) 0 else parent.scopeDepth + 1

    override fun register(value: VMValue): Int {
        val id = values.size
        values.add(value)
        return id
    }

    override fun get(id: Int): VMValue? {
        return values.getOrNull(id)
    }

    override fun getBool(value: Boolean): BooleanValue {
        return if (value) constantValueFactory.TRUE else constantValueFactory.FALSE
    }

    override fun createValue(expr: LuaExpr): VMValue {
        return constantValueFactory.UNKNOWN
    }

    override fun createVariableValue(name: String, psi: LuaPsiElement): VariableValue {
        return namedValues.getOrPut(name) { VariableValue(name, this) }
    }

    override fun findVariable(name: String): VariableValue? {
        return namedValues.getOrElse(name) { parent?.findVariable(name) }
    }

    override fun createLiteralValue(expr: LuaLiteralExpr) = when (expr.kind) {
        LuaLiteralKind.Bool -> if (expr.boolValue) constantValueFactory.TRUE else constantValueFactory.FALSE
        LuaLiteralKind.Nil -> constantValueFactory.NIL
        LuaLiteralKind.Number -> constantValueFactory.create(expr.numberValue)
        LuaLiteralKind.String -> constantValueFactory.create(expr.stringValue)
        else -> constantValueFactory.UNKNOWN
    }
}