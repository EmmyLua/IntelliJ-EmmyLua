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

package com.tang.intellij.lua.debugger.attach

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.MalformedInputException

class IncompleteString {

    private var buffer = ByteArray(0)

    fun append(bytes: ByteArray) {
        buffer += bytes
    }

    fun decode(charset: Charset): String {
        var result: String

        val decoder = charset.newDecoder()
        try {
            result = tryDecode(decoder, buffer.size)
        } catch (e: MalformedInputException) {
            try {
                result = tryDecode(decoder, buffer.size - e.inputLength)
            } catch (e: Exception) {
                result = String(buffer)
                buffer = ByteArray(0)
            }
        }

        return result
    }

    private fun tryDecode(decoder: CharsetDecoder, len: Int): String {
        val buf = ByteBuffer.wrap(buffer, 0, len)
        val result = decoder.decode(buf).toString()
        buffer = buffer.copyOfRange(buf.position(), buffer.size)
        return result
    }
}