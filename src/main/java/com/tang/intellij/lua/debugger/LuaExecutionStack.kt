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

package com.tang.intellij.lua.debugger

import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.impl.frame.XStackFrameContainerEx

/**
 *
 * Created by tangzx on 2016/12/31.
 */
class LuaExecutionStack(private val stackFrameList: List<XStackFrame>) : XExecutionStack("LuaStack") {
    private var _topFrame: XStackFrame? = null

    val stackFrames: Array<XStackFrame>
        get() = stackFrameList.toTypedArray()

    init {
        if (stackFrameList.isNotEmpty())
            _topFrame = stackFrameList[0]
    }

    override fun getTopFrame() = _topFrame

    fun setTopFrame(frame: XStackFrame) {
        _topFrame = frame
    }

    override fun computeStackFrames(i: Int, xStackFrameContainer: XExecutionStack.XStackFrameContainer) {
        val stackFrameContainerEx = xStackFrameContainer as XStackFrameContainerEx
        stackFrameContainerEx.addStackFrames(stackFrameList, topFrame, true)
    }
}
