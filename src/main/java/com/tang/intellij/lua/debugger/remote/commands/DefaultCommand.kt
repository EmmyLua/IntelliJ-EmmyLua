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

import com.tang.intellij.lua.debugger.remote.MobClient

import java.io.IOException

/**
 *
 * Created by tangzx on 2016/12/31.
 */
open class DefaultCommand @JvmOverloads constructor(private val commandline: String, private val requireRespLines: Int = 1) : DebugCommand() {
    internal var handleLines: Int = 0

    @Throws(IOException::class)
    override fun write(writer: MobClient) {
        writer.write(commandline)
    }

    override fun handle(data: String): Int {
        val LB = data.indexOf('\n')
        if (LB == -1) return LB

        handle(handleLines++, data)
        return data.length
    }

    override fun isFinished(): Boolean {
        return requireRespLines <= handleLines
    }

    override fun getRequireRespLines(): Int {
        return requireRespLines
    }

    protected open fun handle(index: Int, data: String) {

    }
}
