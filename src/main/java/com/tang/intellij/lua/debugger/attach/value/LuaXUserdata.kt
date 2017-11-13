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

package com.tang.intellij.lua.debugger.attach.value

import com.intellij.icons.AllIcons
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcessBase

/**
 * user data
 * Created by Administrator on 2017/6/13.
 */
class LuaXUserdata(L: Long, process: LuaAttachDebugProcessBase)
    : LuaXObjectValue(StackNodeId.Error, L, process) {

    override fun computePresentation(xValueNode: XValueNode, xValuePlace: XValuePlace) {
        xValueNode.setPresentation(AllIcons.Json.Object, type, data, true)
    }

    override fun toKeyString(): String {
        return data
    }
}
