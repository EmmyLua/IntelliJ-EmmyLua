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

import com.tang.intellij.lua.debugger.remote.LuaMobDebugProcess
import com.tang.intellij.lua.debugger.remote.MobClient

/**
 * Remote Debug Command
 * Created by tangzx on 2016/12/31.
 */
abstract class DebugCommand {

    lateinit var debugProcess: LuaMobDebugProcess

    abstract fun isFinished(): Boolean

    abstract fun getRequireRespLines(): Int

    abstract fun write(writer: MobClient)

    abstract fun handle(data: String): Int
}
