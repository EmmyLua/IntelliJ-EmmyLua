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
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.impl.XSourcePositionImpl
import com.tang.intellij.lua.debugger.LuaExecutionStack
import com.tang.intellij.lua.debugger.attach.value.LuaXValue
import com.tang.intellij.lua.psi.LuaFileUtil
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

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
    ReqProfilerEnd,
    RespProfilerData,
}

open class LuaAttachMessage(val id: DebugMessageId) {

    var L:Long = 0
        protected set

    lateinit var process: LuaAttachDebugProcess

    open fun write(stream: DataOutputStream) {
        stream.writeInt(id.ordinal)
        stream.writeLong(L)
    }

    open fun read(stream: DataInputStream) {
        L = stream.readLong()
    }

    companion object {
        fun parseMessage(stream: DataInputStream, process: LuaAttachDebugProcess): LuaAttachMessage {
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
                DebugMessageId.RespInitialize,
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

fun DataOutputStream.writeString(s: String) {
    writeInt(s.length)
    write(s.toByteArray())
}

fun DataInputStream.readString(): String {
    val len = this.readInt()
    val bytes = ByteArray(len)
    this.read(bytes)
    return String(bytes)
}

class DMReqInitialize(private val symbolsDirectory: String, private val emmyLuaFile: String)
    : LuaAttachMessage(DebugMessageId.ReqInitialize) {
    override fun write(stream: DataOutputStream) {
        super.write(stream)
        stream.writeString(symbolsDirectory)
        stream.writeString(emmyLuaFile)
    }
}

class DMMessage : LuaAttachMessage(DebugMessageId.Message) {

    companion object {
        val Normal = 0
        val Warning = 1
        val Error = 2
    }

    lateinit var message: String
        private set
    var type: Int = 0
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)
        type = stream.readInt()
        message = stream.readString()
    }

    fun print() {
        val contentType =  when (type) {
            Error -> ConsoleViewContentType.ERROR_OUTPUT
            Warning -> ConsoleViewContentType.LOG_WARNING_OUTPUT
            else -> ConsoleViewContentType.NORMAL_OUTPUT
        }
        process.println(message, contentType)
    }
}

class DMException : LuaAttachMessage(DebugMessageId.Exception) {
    lateinit var message: String
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)
        message = stream.readString()
    }

    fun print() {
        process.println(message, ConsoleViewContentType.ERROR_OUTPUT)
    }
}

class DMLoadScript : LuaAttachMessage(DebugMessageId.LoadScript) {
    lateinit var fileName: String
        private set
    lateinit var source: String
        private set
    var index: Int = 0
        private set
    var state: Int = 0
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)
        fileName = stream.readString()
        source = stream.readString()
        index = stream.readInt()
        state = stream.readInt()
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
    private lateinit var stackXML: String
    lateinit var stack: LuaExecutionStack
        private set
    var name: String? = null
        private set
    var line: Int = 0
        private set

    override fun read(stream: DataInputStream) {
        super.read(stream)
        stackXML = stream.readString()
        try {
            val builderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = builderFactory.newDocumentBuilder()

            val document = documentBuilder.parse(ByteArrayInputStream(stackXML.toByteArray(charset("UTF-8"))))
            val root = document.documentElement
            parseStack(root)
        } catch (e: Exception) {
            println("Parse exception:")
            println(stackXML)
        }
    }

    private fun parseStack(item: Element) {
        val frames = ArrayList<XStackFrame>()
        val nodeList = item.getElementsByTagName("stack")
        for (stackIndex in 0 until nodeList.length) {
            val stackNode = nodeList.item(stackIndex)
            val attributes = stackNode.attributes
            val functionNode = attributes.getNamedItem("function")
            val scriptIndexNode = attributes.getNamedItem("script_index")
            val lineNode = attributes.getNamedItem("line")

            val script = process.getScript(Integer.parseInt(scriptIndexNode.textContent))
            var scriptName: String? = null
            val line = Integer.parseInt(lineNode.textContent)
            var position: XSourcePosition? = null
            if (script != null) {
                scriptName = script.name
                // find source position
                val file = LuaFileUtil.findFile(process.session?.project!!, scriptName)
                if (file != null) {
                    position = XSourcePositionImpl.create(file, line)

                    if (name == null) {
                        this.line = line
                        this.name = scriptName
                    }
                }
            }
            val childrenList = parseValue(stackNode)
            val frame = LuaAttachStackFrame(this, childrenList, position, functionNode.textContent, scriptName, stackIndex)
            frames.add(frame)
        }
        stack = LuaExecutionStack(frames)
    }

    private fun parseValue(stackNode: Node): XValueChildrenList {
        val list = XValueChildrenList()
        var valueNode: Node? = stackNode.firstChild
        while (valueNode != null) {
            if (valueNode is Element) {
                val value = LuaXValue.parse(valueNode, L, process)
                var name = "unknown"
                val valueNodeChildNodes = valueNode.childNodes
                for (i in 0 until valueNodeChildNodes.length) {
                    val item = valueNodeChildNodes.item(i)
                    if (item.nodeName == "name") {
                        name = item.textContent
                        break
                    }
                }
                value.name = name
                list.add(name, value)
            }
            valueNode = valueNode.nextSibling
        }
        return list
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

    var xValue: LuaXValue? = null
        private set
    var success: Boolean = false
        private set
    var evalId: Int = 0
        private set
    private lateinit var result: String

    override fun read(stream: DataInputStream) {
        super.read(stream)

        success = stream.readInt() == 1
        evalId = stream.readInt()
        result = stream.readString()
        xValue = LuaXValue.parse(result, L, process)
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
            val file = stream.readString()
            val function = stream.readString()
            val line = stream.readInt()
            val count = stream.readInt()
            val time = stream.readInt()
            val call = DMProfilerCall(id, file, function, line, count, time)
            list.add(call)
        }
    }
}