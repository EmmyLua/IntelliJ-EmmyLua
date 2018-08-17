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

import com.intellij.openapi.application.ApplicationManager
import java.util.regex.Pattern

/**
 *
 * Created by tangzx on 2017/1/1.
 */
class EvaluatorCommand(expr: String, private val callback: Callback) : DefaultCommand("EXEC $expr --{maxlevel=1}", 2) {
    private var hasError2Process: Boolean = false
    private var dataLen: Int = 0
    private val dataBuffer = StringBuffer()

    interface Callback {
        fun onResult(data: String)
    }

    override fun isFinished(): Boolean {
        return !hasError2Process && super.isFinished()
    }

    /*private fun createExpr(chunk: String, getChildren: Boolean): String {
        val serFN = "local function se(o, children) " +
                "if type(o) == 'string' then return { nil, o, 'string' } " +
                "elseif type(o) == 'number' then return { nil, o, 'number' } " +
                "elseif type(o) == 'table' then if not children then return { nil, tostring(o), 'table' } end; " +
                "local r = {} " +
                "for k, v in pairs(o) do " +
                "r[k] = { k, tostring(v), type(v) } " +
                "end return r " +
                "elseif type(o) == 'function' then return { nil, tostring(o), 'function' } " +
                "end end "
        val exec = String.format("local function exec() %s end local data = exec() return se(data, %b)", chunk, getChildren)
        return serFN + exec
    }*/

    override fun handle(data: String): Int {
        if (hasError2Process && dataLen > 0) {
            hasError2Process = false
            handleLines++
            val error = data.substring(0, dataLen)
            debugProcess.error(error)
            onResult("do local _={\"\\\"401_error_happened\\\"\"};return _;end")
            return dataLen
        }
        if (dataLen != 0) {
            val index = data.indexOf("return _;end")
            return if (index > 0) {
                dataBuffer.append(data.substring(0, index + 12))
                val code = dataBuffer.toString()
                handleLines++
                onResult(code)
                index + 12
            } else {
                dataBuffer.append(data)
                data.length
            }
        }
        return super.handle(data)
    }

    private fun onResult(code: String) {
        try {
            ApplicationManager.getApplication().runReadAction { callback.onResult(code) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun handle(index: Int, data: String) {
        if (data.startsWith("401")) {
            hasError2Process = true
            val pattern = Pattern.compile("(\\d+)([^\\d]+)(\\d+)")
            val matcher = pattern.matcher(data)
            if (matcher.find()) {
                dataLen = matcher.group(3).toInt()
            }
            return
        }
        if (data.startsWith("400")) {
            hasError2Process = false
            handleLines++
            dataLen = 0
            onResult("do local _={\"\\\"400_bad_request\\\"\"};return _;end")
        }
        if (data.startsWith("200 OK")) {
            val pattern = Pattern.compile("\\d+[^\\d]+(\\d+)")
            val matcher = pattern.matcher(data)
            if (matcher.find()) {
                dataLen = matcher.group(1).toInt()
            }
        }
    }
}
