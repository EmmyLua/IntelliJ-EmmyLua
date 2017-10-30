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

import com.tang.intellij.lua.debugger.attach.protos.LuaAttachProto
import java.io.DataOutputStream

open class LuaAttachMessage {
    open fun write(stream: DataOutputStream) {

    }
}

fun DataOutputStream.writeString(s: String) {
    writeInt(s.length)
    write(s.toByteArray())
}

class InitMessage : LuaAttachMessage() {
    override fun write(stream: DataOutputStream) {
        stream.writeInt(LuaAttachProto.CommandId_Initialize)
        stream.writeString("123")
    }
}

class LuaAddBPMessage : LuaAttachMessage() {

}