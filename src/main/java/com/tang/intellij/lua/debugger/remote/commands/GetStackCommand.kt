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

package com.tang.intellij.lua.debugger.remote.commands

import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.impl.XSourcePositionImpl
import com.tang.intellij.lua.debugger.LuaExecutionStack
import com.tang.intellij.lua.debugger.remote.LuaMobStackFrame
import com.tang.intellij.lua.debugger.remote.value.LuaRValue
import com.tang.intellij.lua.psi.LuaFileUtil
import org.luaj.vm2.LuaTable
import org.luaj.vm2.lib.jse.JsePlatform
import java.util.*
import java.util.regex.Pattern

/**
 *
 * Created by tangzx on 2016/12/31.
 */
class GetStackCommand : DefaultCommand("STACK --{maxlevel=10}", 1) {

    private var hasError: Boolean = false
    private var errorDataLen: Int = 0

    override fun isFinished(): Boolean {
        return !hasError && super.isFinished()
    }

    override fun handle(data: String): Int {
        if (hasError) {
            hasError = false
            val error = data.substring(0, errorDataLen)
            debugProcess.error(error)
            debugProcess.runCommand(DefaultCommand("RUN", 0))
            return errorDataLen
        }
        return super.handle(data)
    }

    override fun handle(index: Int, data: String) {
        if (data.startsWith("401")) {
            hasError = true
            val pattern = Pattern.compile("(\\d+)([^\\d]+)(\\d+)")
            val matcher = pattern.matcher(data)
            if (matcher.find()) {
                errorDataLen = Integer.parseInt(matcher.group(3))
            }
            return
        }

        if (data.startsWith("200 OK")) {
            val stackCode = data.substring(6)
            val standardGlobals = JsePlatform.standardGlobals()
            val strippedCode = limitStringSize(stackCode)
            val code = standardGlobals.load(strippedCode)
            val function = code.checkfunction()
            val value = function.call()

            val frames = ArrayList<XStackFrame>()
            for (i in 1..value.length()) {
                val stackValue = value.get(i)
                val stackInfo = stackValue.get(1)

                val funcName = stackInfo.get(1)
                val fileName = stackInfo.get(2)
                val line = stackInfo.get(4)

                var position: XSourcePositionImpl? = null
                val virtualFile = LuaFileUtil.findFile(debugProcess.session.project, fileName.toString())
                if (virtualFile != null) {
                    val nLine = line.toint()
                    position = XSourcePositionImpl.create(virtualFile, nLine - 1)
                }

                var functionName = funcName.toString()
                if (funcName.isnil())
                    functionName = "main"

                val frame = LuaMobStackFrame(functionName, position, debugProcess)

                parseValues(stackValue.get(2).checktable(), frame)
                parseValues(stackValue.get(3).checktable(), frame)

                frames.add(frame)
            }
            debugProcess.setStack(LuaExecutionStack(frames))
        }
    }

    private fun parseValues(paramsTable: LuaTable, frame: LuaMobStackFrame) {
        val keys = paramsTable.keys()
        for (key in keys) {
            val luaValue = paramsTable.get(key)
            val desc = luaValue.get(2)
            val xValue = LuaRValue.create(key.toString(), luaValue.get(1), desc.toString(), debugProcess.session)
            frame.addValue(xValue)
        }
    }
}