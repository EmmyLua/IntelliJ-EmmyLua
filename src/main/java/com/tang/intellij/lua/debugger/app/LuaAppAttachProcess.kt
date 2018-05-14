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

package com.tang.intellij.lua.debugger.app

import com.intellij.xdebugger.XDebugSession
import com.tang.intellij.lua.debugger.attach.LuaAttachBridgeBase
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcessBase
import java.nio.charset.Charset

/**
 *
 * Created by tangzx on 2017/5/7.
 */
class LuaAppAttachProcess(session: XDebugSession) : LuaAttachDebugProcessBase(session) {

    private var _charset: Charset? = null

    override val charset: Charset
        get() = _charset ?: super.charset

    override fun startBridge(): LuaAttachBridgeBase {
        val configuration = session.runProfile as LuaAppRunConfiguration
        _charset = Charset.forName(configuration.charset)

        val bridge = LuaAppAttachBridge(this, session)
        this.bridge = bridge
        bridge.setProtoHandler(this)
        bridge.launch(configuration.program, configuration)
        this.emmyInputEnabled = !configuration.showConsole
        return bridge
    }
}
