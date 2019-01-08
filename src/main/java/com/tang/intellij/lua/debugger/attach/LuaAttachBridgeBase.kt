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

import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.tang.intellij.lua.debugger.LogConsoleType
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.LuaFileUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.*

/**
 * debug bridge
 * Created by tangzx on 2017/3/26.
 */
abstract class LuaAttachBridgeBase(val process: LuaAttachDebugProcessBase, val session: XDebugSession) {
    protected var handler: OSProcessHandler? = null
    private var writer: DataOutputStream? = null
    private var protoHandler: ProtoHandler? = null
    private var evalIdCounter = 0
    private val callbackMap = HashMap<Int, EvalInfo>()
    private var socket = Socket()
    /**
     * capture stderr & stdout
     */
    var captureStd = LuaSettings.instance.attachDebugCaptureStd

    var captureOutput = LuaSettings.instance.attachDebugCaptureOutput

    protected val emmyLua: String?
        get() = LuaFileUtil.getPluginVirtualFile("debugger/Emmy.lua")

    fun setProtoHandler(protoHandler: ProtoHandler) {
        this.protoHandler = protoHandler
    }

    interface ProtoHandler {
        fun handle(message: LuaAttachMessage)
    }

    interface EvalCallback {
        fun onResult(result: DMRespEvaluate)
    }

    internal inner class EvalInfo {
        var callback: EvalCallback? = null
        var expr: String? = null
    }

    protected fun onDebugHelperExit(code: Int) {
        val errorCode = getDebugHelperExitCode(code)
        when (errorCode) {
            ErrorCode.OK, ErrorCode.ALREADY_ATTACHED -> {

            }
            ErrorCode.INJECT_ERROR -> {
                process.error("Error: emmy.backend.dll could not be loaded into the process")
                process.stop()
            }
            ErrorCode.BACKEND_INIT_ERROR -> {
                process.error("Error: Backend couldn't be initialized")
                process.stop()
            }
            ErrorCode.CAN_NOT_OPEN_PROCESS -> {
                process.error("Error: The process could not be opened")
                process.stop()
            }
            else -> {
                //process.error("Error: Unknown error")
                process.stop()
            }
        }
    }

    protected open fun handleMessage(message: LuaAttachMessage) {
        when (message.id ) {
            DebugMessageId.RespEvaluate -> handleEvalCallback(message as DMRespEvaluate)
            else -> protoHandler?.handle(message)
        }
    }

    private fun handleEvalCallback(proto: DMRespEvaluate) {
        val info = callbackMap.remove(proto.evalId)
        if (info != null) {
            val xValue = proto.resultNode.value
            xValue.name = info.expr
            info.callback?.onResult(proto)
        }
    }

    protected fun connect(port: Int) {
        try {
            socket = Socket()
            socket.tcpNoDelay = true
            socket.connect(InetSocketAddress("localhost", port))
            writer = DataOutputStream(socket.getOutputStream())

            ApplicationManager.getApplication().executeOnPooledThread {
                send(DMReqInitialize("", emmyLua!!, captureStd, captureOutput))
                processPack()
            }
        } catch (e: Exception) {
            process.error(e.message!!)
        }
    }

    private fun processPack() {
        try {
            val inputStream = socket.getInputStream()
            while (true) {
                val lenBytes = ByteArray(4)
                inputStream.read(lenBytes)
                val len = DataInputStream(ByteArrayInputStream(lenBytes)).readInt()
                val bytes = ByteArray(len)
                var read = 0
                while (read < len) {
                    val r = inputStream.read(bytes, read, len - read)
                    read += r
                }
                handleMsg(bytes)
            }
        } catch (e: SocketException) {
            process.println("debugger disconnected.", LogConsoleType.NORMAL, ConsoleViewContentType.SYSTEM_OUTPUT)
            session.stop()
        } catch (e: Exception) {
            e.message?.let { process.error(it, LogConsoleType.NORMAL) }
            session.stop()
        }
    }

    private fun handleMsg(byteArray: ByteArray) {
        val reader = DataInputStream(ByteArrayInputStream(byteArray))
        val message = LuaAttachMessage.parseMessage(reader, process)
        handleMessage(message)
    }

    fun stop() {
        writer = null
        handler?.destroyProcess()
        handler = null
        socket.close()
    }

    fun send(message: LuaAttachMessage) {
        val byte = ByteArrayOutputStream()
        val stream = DataOutputStream(byte)
        message.write(stream)
        val byteArray = byte.toByteArray()

        val byte2 = ByteArrayOutputStream()
        val stream2 = DataOutputStream(byte2)
        stream2.writeInt(byteArray.size)
        stream2.write(byteArray)

        socket.getOutputStream().write(byte2.toByteArray())
        socket.getOutputStream().flush()
    }

    fun eval(L: Long, expr: String, stack: Int, depth: Int, callback: EvalCallback) {
        val id = evalIdCounter++
        val info = EvalInfo()
        info.callback = callback
        info.expr = expr
        callbackMap[id] = info
        send(DMReqEvaluate(L, id, stack, depth, expr))
    }

    fun addBreakpoint(index: Int, breakpoint: XLineBreakpoint<*>) {
        val expression = breakpoint.conditionExpression
        val exp = expression?.expression ?: ""
        send(DMAddBreakpoint(index, breakpoint.line, exp))
    }

    fun removeBreakpoint(index: Int, breakpoint: XLineBreakpoint<*>) {
        send(DMDelBreakpoint(index, breakpoint.line))
    }

    fun sendDone() {
        send(LuaAttachMessage(DebugMessageId.LoadDone))
    }

    fun sendRun() {
        send(LuaAttachMessage(DebugMessageId.Continue))
    }
}