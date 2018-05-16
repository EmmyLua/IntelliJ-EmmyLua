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

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.util.PathUtil
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.impl.XSourcePositionImpl
import com.tang.intellij.lua.debugger.LogConsoleType
import com.tang.intellij.lua.debugger.LuaExecutionStack
import com.tang.intellij.lua.debugger.attach.value.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.charset.Charset

enum class ErrorCode
{
    OK,

    UNKNOWN,
    CAN_NOT_OPEN_PROCESS,
    ALREADY_ATTACHED,
    INJECT_ERROR,
    BACKEND_INIT_ERROR
}

fun getDebugHelperExitCode(value: Int): ErrorCode {
    return ErrorCode.values().find { it.ordinal == value } ?: ErrorCode.UNKNOWN
}

enum class DebugMessageId
{
    ReqInitialize,
    RespInitialize,

    Continue,
    StepOver,
    StepInto,
    StepOut,
    AddBreakpoint,
    DelBreakpoint,
    Break,
    Detach,
    PatchReplaceLine,
    PatchInsertLine,
    PatchDeleteLine,
    LoadDone,
    IgnoreException,
    DeleteAllBreakpoints,

    CreateVM,
    NameVM,
    DestroyVM,
    LoadScript,
    SetBreakpoint,
    Exception,
    LoadError,
    Message,
    SessionEnd,

    ReqEvaluate,
    RespEvaluate,

    ReqProfilerBegin,
    RespProfilerBegin,
    ReqProfilerEnd,
    RespProfilerEnd,
    RespProfilerData,

    ReqReloadScript,
    RespReloadScript,

    ReqStdin,
}

open class LuaAttachMessage(val id: DebugMessageId) {

    var L:Long = 0
        protected set

    lateinit var process: LuaAttachDebugProcessBase

    open fun write(stream: DataOutputStream) {
        stream.writeInt(id.ordinal)
        stream.writeLong(L)
    }

    open fun read(stream: DataInputStream) {
        L = stream.readLong()
    }

    companion object {
        fun parseMessage(stream: DataInputStream, process: LuaAttachDebugProcessBase): LuaAttachMessage {
            val id = stream.readInt()
            val idType = DebugMessageId.values().find { it.ordinal == id }
            val m:LuaAttachMessage = when (idType) {
                DebugMessageId.LoadScript -> DMLoadScript()
                DebugMessageId.Message -> DMMessage()
                DebugMessageId.Exception -> DMException()
                DebugMessageId.Break -> DMBreak()
                DebugMessageId.SetBreakpoint -> DMSetBreakpoint()
                DebugMessageId.RespEvaluate -> DMRespEvaluate()
                DebugMessageId.RespProfilerData -> DMRespProfilerData()
                DebugMessageId.LoadError -> DMLoadError()
                DebugMessageId.RespInitialize,
                DebugMessageId.RespProfilerBegin,
                DebugMessageId.RespProfilerEnd,
                DebugMessageId.DestroyVM,
                DebugMessageId.CreateVM -> LuaAttachMessage(idType)
                else -> {
                    throw Exception("unknown message id:$idType")
                }
            }
            m.process = process
            m.read(stream)
            return m
        }
    }
}

fun DataOutputStream.writeString(s: String, charset: Charset = Charset.defaultCharset()) {
    val array = s.toByteArray(charset)
    writeInt(array.size)
    write(array)
}

fun DataOutputStream.writeSize(size: Long) {
    writeLong(size)
}

fun DataInputStream.readString(charset: Charset): String {
    val len = this.readInt()
    val bytes = ByteArray(len)
    this.read(bytes)

    return String(bytes, charset)
}

fun DataInputStream.readSize(): Long {
    return readLong()
}

class DMReqInitialize(private val symbolsDirectory: String,
                      private val emmyLuaFile: String,
                      private val captureStd: Boolean = false,
                      private val captureOutputDebugString: Boolean = true)
    : LuaAttachMessage(DebugMessageId.ReqInitialize) {
    override fun write(stream: DataOutputStream) {
        super.write(stream)
        stream.writeString(symbolsDirectory)
        stream.writeString(emmyLuaFile)
        stream.writeBoolean(captureStd)
        stream.writeBoolean(captureOutputDebugString)
    }
}

class DMMessage : LuaAttachMessage(DebugMessageId.Message) {

    companion object {
        const val Normal = 0
        const val Warning = 1
        const val Error = 2
        const val Stdout = 3
        const val Stderr = 4
    }

    val message: String by lazy { String(bytes, process.charset) }

    lateinit var bytes: ByteArray
        private set
    var type: Int = 0
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)
        type = stream.readInt()
        val len = stream.readInt()
        bytes = stream.readBytes(len)
    }

    fun print() {
        when (type) {
            Error -> process.println(message, LogConsoleType.EMMY, ConsoleViewContentType.ERROR_OUTPUT)
            Warning -> process.println(message, LogConsoleType.EMMY, ConsoleViewContentType.LOG_WARNING_OUTPUT)
            Stdout -> process.print(message, LogConsoleType.NORMAL, ConsoleViewContentType.SYSTEM_OUTPUT)
            Stderr -> process.print(message, LogConsoleType.NORMAL, ConsoleViewContentType.ERROR_OUTPUT)
            else -> process.println(message, LogConsoleType.EMMY, ConsoleViewContentType.NORMAL_OUTPUT)
        }
    }
}

class DMException : LuaAttachMessage(DebugMessageId.Exception) {
    lateinit var message: String
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)
        message = stream.readString(process.charset)
    }

    fun print() {
        process.error(message, LogConsoleType.NORMAL)
    }
}

class DMLoadScript : LuaAttachMessage(DebugMessageId.LoadScript) {
    lateinit var fileName: String
        private set
    lateinit var source: String
        private set
    var index: Int = 0
        private set
    var state: CodeState = CodeState.Normal
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)
        fileName = PathUtil.getCanonicalPath(stream.readString(process.charset))
        source = stream.readString(process.charset)
        index = stream.readInt()

        val stateCode = stream.readByte()
        this.state = CodeState.values().find { it.ordinal.toByte() == stateCode } ?: CodeState.Normal
    }
}

class DMLoadError : LuaAttachMessage(DebugMessageId.LoadError) {
    var message: String = ""
    override fun read(stream: DataInputStream) {
        super.read(stream)
        message = stream.readString(process.charset)
    }
}

class DMSetBreakpoint : LuaAttachMessage(DebugMessageId.SetBreakpoint) {
    var scriptIndex: Int = 0
        private set
    var line: Int = 0
        private set
    var success: Boolean = false
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)
        scriptIndex = stream.readInt()
        line = stream.readInt()
        success = stream.readInt() == 1
    }
}

class DMAddBreakpoint(private val scriptIndex: Int,
                      private val line: Int,
                      private val expr: String) : LuaAttachMessage(DebugMessageId.AddBreakpoint) {
    override fun write(stream: DataOutputStream) {
        super.write(stream)
        stream.writeInt(scriptIndex)
        stream.writeInt(line)
        stream.writeString(expr)
    }
}

class DMBreak : LuaAttachMessage(DebugMessageId.Break) {
    private lateinit var stacks: StackNodeContainer
    lateinit var stack: LuaExecutionStack
        private set
    var name: String? = null
        private set
    var line: Int = 0
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)
        val frames = mutableListOf<XStackFrame>()
        stacks = readNode(stream, L, process) as StackNodeContainer
        var stackIndex = 0
        stacks.children.forEach {
            val stack = it as StackRootNode
            val childList = XValueChildrenList()
            stack.children.forEach {
                val value = it as LuaXValue
                childList.add(value.name, value)
            }

            val script = process.getScript(stack.scriptIndex)
            var position: XSourcePosition? = null
            if (script != null) {
                val file = process.findFile(script.name)
                if (file != null) {
                    position = XSourcePositionImpl.create(file, stack.line)

                    if (name == null) {
                        this.line = stack.line
                        this.name = script.name
                    }
                }
            }

            val frame = LuaAttachStackFrame(this,
                    childList,
                    position,
                    stack.functionName,
                    script?.name,
                    stackIndex++)
            frames.add(frame)
        }
        stack = LuaExecutionStack(frames)
    }
}

class DMDelBreakpoint(private val scriptIndex: Int,
                      private val line: Int) : LuaAttachMessage(DebugMessageId.DelBreakpoint) {
    override fun write(stream: DataOutputStream) {
        super.write(stream)
        stream.writeInt(scriptIndex)
        stream.writeInt(line)
    }
}

class DMReqEvaluate(L: Long, private val evalId: Int,
                    private val stackLevel: Int,
                    private val depth: Int,
                    private val expr: String) : LuaAttachMessage(DebugMessageId.ReqEvaluate) {
    init {
        this.L = L
    }
    override fun write(stream: DataOutputStream) {
        super.write(stream)
        stream.writeInt(evalId)
        stream.writeInt(stackLevel)
        stream.writeInt(depth)
        stream.writeString(expr)
    }
}

class DMRespEvaluate : LuaAttachMessage(DebugMessageId.RespEvaluate) {

    lateinit var resultNode: EvalResultNode
        private set
    var success: Boolean = false
        private set
    var evalId: Int = 0
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)

        evalId = stream.readInt()
        success = stream.readBoolean()
        resultNode = readNode(stream, L, process) as EvalResultNode
    }
}

data class DMProfilerCall(val id: Int,
                          val file: String,
                          val functionName: String,
                          val line: Int,
                          var count: Int,
                          var time: Int)

class DMRespProfilerData : LuaAttachMessage(DebugMessageId.RespProfilerData) {

    val list = mutableListOf<DMProfilerCall>()

    override fun read(stream: DataInputStream) {
        super.read(stream)
        val size = stream.readInt()
        for (i in 0 until size) {
            val id = stream.readInt()
            val file = PathUtil.getCanonicalPath(stream.readString(process.charset))
            val function = stream.readString(process.charset)
            val line = stream.readInt()
            val count = stream.readInt()
            val time = stream.readInt()
            val call = DMProfilerCall(id, file, function, line, count, time)
            list.add(call)
        }
    }
}

class DMReqReloadScript(private val index: Int) : LuaAttachMessage(DebugMessageId.ReqReloadScript) {
    override fun write(stream: DataOutputStream) {
        super.write(stream)
        stream.writeInt(index)
    }
}

class DMStdin(private val text: String) : LuaAttachMessage(DebugMessageId.ReqStdin) {
    override fun write(stream: DataOutputStream) {
        super.write(stream)
        stream.writeString(text, Charset.forName("GBK"))
    }
}