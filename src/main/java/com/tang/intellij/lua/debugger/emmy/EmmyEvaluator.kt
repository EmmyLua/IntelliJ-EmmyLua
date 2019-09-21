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

package com.tang.intellij.lua.debugger.emmy

import com.intellij.xdebugger.XSourcePosition
import com.tang.intellij.lua.debugger.LuaDebuggerEvaluator
import com.tang.intellij.lua.debugger.emmy.value.LuaXValue

class EmmyEvaluator(val frame: EmmyDebugStackFrame, val process: EmmyDebugProcessBase) : LuaDebuggerEvaluator(), IEvalResultHandler {

    private val callbackMap = mutableMapOf<Int, XEvaluationCallback>()

    init {
        process.addEvalResultHandler(this)
    }

    override fun handleMessage(msg: EvalRsp) {
        val callback = callbackMap[msg.seq]
        if (callback != null) {
            if (msg.success)
                callback.evaluated(LuaXValue.create(msg.value!!, frame))
            else
                callback.errorOccurred(msg.error ?: "unknown error")
            callbackMap.remove(msg.seq)
        }
    }

    fun eval(express: String, cacheId: Int, xEvaluationCallback: XEvaluationCallback, depth: Int = 1) {
        val req = EvalReq(express, frame.data.level, cacheId, depth)
        process.send(req)
        callbackMap[req.seq] = xEvaluationCallback
    }

    override fun eval(express: String, xEvaluationCallback: XEvaluationCallback, xSourcePosition: XSourcePosition?) {
        eval(express, 0, xEvaluationCallback)
    }
}