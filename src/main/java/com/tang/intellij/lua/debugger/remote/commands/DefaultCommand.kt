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
import com.tang.intellij.lua.lexer.LuaLexerAdapter
import com.tang.intellij.lua.psi.LuaTypes
import java.io.IOException

/**
 *
 * Created by tangzx on 2016/12/31.
 */
open class DefaultCommand @JvmOverloads constructor(private val commandline: String, private val requireRespLines: Int = 1) : DebugCommand() {
    internal var handleLines: Int = 0

    var lineBuffer = StringBuffer(1024 * 32)

    @Throws(IOException::class)
    override fun write(writer: MobClient) {
        writer.write(commandline)
    }

    override fun handle(data: String): Int {
        val lb = data.indexOf('\n')
        return if (lb == -1) {
            lineBuffer.append(data, 0, data.length)
            data.length
        } else {
            lineBuffer.append(data, 0, lb + 1)
            handle(handleLines++, lineBuffer.toString())
            lineBuffer.setLength(0)
            lb + 1
        }
    }

    override fun isFinished(): Boolean {
        return requireRespLines <= handleLines
    }

    override fun getRequireRespLines(): Int {
        return requireRespLines
    }

    protected open fun handle(index: Int, data: String) {

    }

    data class StringRange(val start: Int, val end: Int)

    protected fun limitStringSize(code: String, limit: Int = 1000): String {
        val lex = LuaLexerAdapter()
        lex.start(code)
        val rangeList = mutableListOf<StringRange>()
        while (true) {
            lex.advance()
            if (lex.tokenType == LuaTypes.STRING) {
                val len = lex.tokenEnd - lex.tokenStart
                if (len > limit) {
                    rangeList.add(StringRange(lex.tokenStart, lex.tokenEnd))
                }
            }
            if (lex.bufferEnd == lex.tokenEnd)
                break
        }
        if (rangeList.isNotEmpty()) {
            return buildString {
                var prev = 0
                rangeList.forEach { range ->
                    append(code.substring(prev, range.start + limit))
                    append("...(Emmy : stripped size = ${range.end - range.start - limit})")
                    prev = range.end - 1
                }
                append(code.substring(prev))
            }
        }
        return code
    }
}
