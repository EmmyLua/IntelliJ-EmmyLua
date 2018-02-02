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

package com.tang.intellij.lua.debugger.remote.value

import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.frame.XNamedValue
import com.intellij.xdebugger.frame.XNavigatable
import com.tang.intellij.lua.debugger.attach.value.LuaXValue
import org.luaj.vm2.LuaValue

/**
 * remote value
 * Created by tangzx on 2017/4/16.
 */
abstract class LuaRValue(name: String) : XNamedValue(name) {

    protected lateinit var session: XDebugSession

    protected abstract fun parse(data: LuaValue, desc: String)

    var parent: LuaRValue? = null

    override fun computeSourcePosition(xNavigable: XNavigatable) {
        LuaXValue.computeSourcePosition(xNavigable, name, session)
    }

    companion object {

        fun create(name: String, data: LuaValue, desc: String, session: XDebugSession): LuaRValue {
            var describe = desc
            val value: LuaRValue = when {
                data.istable() -> LuaRTable(name)
                data.isfunction() -> LuaRFunction(name)
                data.isnil() -> {
                    describe = "nil"
                    LuaRPrimitive(name)
                }
                else -> LuaRPrimitive(name)
            }

            value.session = session
            value.parse(data, describe)
            return value
        }
    }
}
