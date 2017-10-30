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

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Key
import com.intellij.util.io.BinaryOutputReader
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.tang.intellij.lua.LuaBundle
import com.tang.intellij.lua.debugger.DebugLogger
import com.tang.intellij.lua.debugger.attach.protos.LuaAttachEvalResultProto
import com.tang.intellij.lua.debugger.attach.protos.LuaAttachProto
import com.tang.intellij.lua.psi.LuaFileUtil
import org.w3c.dom.Element
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Future
import javax.xml.parsers.DocumentBuilderFactory

/**
 * debug bridge
 * Created by tangzx on 2017/3/26.
 */
class LuaAttachBridge(private val logger: DebugLogger, private val session: XDebugSession) {
    private var handler: OSProcessHandler? = null
    private var writer: DataOutputStream? = null
    private var protoHandler: ProtoHandler? = null
    private var protoFactory: ProtoFactory? = null
    private var evalIdCounter = 0
    private val callbackMap = HashMap<Int, EvalInfo>()
    private lateinit var socket: Socket
    private lateinit var reader: ProtoReader
    private lateinit var receiveBuffer: ByteArrayOutputStream

    inner class ProtoReader(stream: InputStream) : BinaryOutputReader(stream, SleepingPolicy.BLOCKING) {
        init {
            start("Lua attach debugger proto reader")
        }
        override fun executeOnPooledThread(runnable: Runnable): Future<*> {
            return ApplicationManager.getApplication().executeOnPooledThread(runnable)
        }

        override fun onBinaryAvailable(data: ByteArray, size: Int) {
            receive(data, size)
        }
    }

    private val processListener = object : ProcessListener {
        private var readProto = false
        private var sb: StringBuilder? = null

        override fun startNotified(processEvent: ProcessEvent) {

        }

        override fun processTerminated(processEvent: ProcessEvent) {
            stop(false)
        }

        override fun processWillTerminate(processEvent: ProcessEvent, b: Boolean) {

        }

        override fun onTextAvailable(processEvent: ProcessEvent, key: Key<*>) {
            if (key === ProcessOutputTypes.STDOUT) {
                val line = processEvent.text
                if (readProto) {
                    if (line.startsWith("[end]")) {
                        readProto = false
                        val data = sb!!.toString()
                        val proto = parse(data)
                        if (proto != null)
                            handleProto(proto)
                    } else {
                        sb!!.append(line)
                    }
                } else {
                    readProto = line.startsWith("[start]")
                    if (readProto) {
                        sb = StringBuilder()
                    }
                }
            }
        }
    }

    private val emmyLua: String?
        get() = LuaFileUtil.getPluginVirtualFile("debugger/Emmy.lua")

    fun setProtoHandler(protoHandler: ProtoHandler) {
        this.protoHandler = protoHandler
    }

    fun setProtoFactory(protoFactory: ProtoFactory) {
        this.protoFactory = protoFactory
    }

    interface ProtoHandler {
        fun handle(proto: LuaAttachProto)
    }

    interface ProtoFactory {
        fun createProto(type: Int): LuaAttachProto
    }

    interface EvalCallback {
        fun onResult(result: LuaAttachEvalResultProto)
    }

    internal inner class EvalInfo {
        var callback: EvalCallback? = null
        var expr: String? = null
    }

    private fun handleProto(proto: LuaAttachProto?) {
        if (proto!!.type == LuaAttachProto.EvalResult) {
            handleEvalCallback(proto as LuaAttachEvalResultProto)
        } else if (protoHandler != null)
            protoHandler!!.handle(proto)
    }

    private fun handleEvalCallback(proto: LuaAttachEvalResultProto) {
        val info = callbackMap.remove(proto.evalId)
        if (info != null) {
            val xValue = proto.xValue
            if (xValue != null)
                xValue.name = info.expr
            info.callback!!.onResult(proto)
        }
    }

    fun attach(processInfo: ProcessInfo) {
        val pid = processInfo.pid.toString()
        val pluginVirtualDirectory = LuaFileUtil.getPluginVirtualDirectory()
        try {
            if (pluginVirtualDirectory != null) {
                // check arch
                val archExe = LuaFileUtil.getPluginVirtualFile("debugger/windows/Arch.exe")
                val processBuilder = ProcessBuilder(archExe!!)
                val isX86: Boolean
                val archChecker = processBuilder.command(archExe, "-pid", pid).start()
                archChecker.waitFor()
                val exitValue = archChecker.exitValue()
                isX86 = exitValue == 1

                val archType = if (isX86) "x86" else "x64"
                logger.println(LuaBundle.message("run.attach.start_info", processInfo.executableName, pid, archType), ConsoleViewContentType.SYSTEM_OUTPUT)
                // attach debugger
                val exe = LuaFileUtil.getPluginVirtualFile(String.format("debugger/windows/%s/Debugger.exe", archType))

                val commandLine = GeneralCommandLine(exe!!)
                commandLine.addParameters("-m", "attach", "-p", pid, "-e", emmyLua)
                commandLine.charset = Charset.forName("UTF-8")
                handler = OSProcessHandler(commandLine)
                handler!!.addProcessListener(processListener)
                handler!!.startNotify()

                socket = Socket()
                socket.tcpNoDelay = true
                socket.connect(InetSocketAddress("localhost", processInfo.pid))
                writer = DataOutputStream(socket.getOutputStream())
                receiveBuffer = ByteArrayOutputStream()
                //reader = ProtoReader(socket.getInputStream())
                ApplicationManager.getApplication().executeOnPooledThread {
                    procPack()
                }
                send(InitMessage())
            }
        } catch (e: Exception) {
            logger.error(e.message!!)
            session.stop()
        }

    }

    fun launch(program: String, workingDir: String?, args: Array<String>?) {
        val pluginVirtualDirectory = LuaFileUtil.getPluginVirtualDirectory()
        try {
            if (pluginVirtualDirectory != null) {
                if (workingDir == null || workingDir.isEmpty()) {
                    throw Exception("Working directory not found.")
                }

                // check arch
                val archExe = LuaFileUtil.getPluginVirtualFile("debugger/windows/Arch.exe")
                val processBuilder = ProcessBuilder(archExe!!)
                val isX86: Boolean
                val archChecker = processBuilder.command(archExe, "-file", program).start()
                archChecker.waitFor()
                val exitValue = archChecker.exitValue()
                if (exitValue == -1) {
                    throw Exception(String.format("Program [%s] not found.", program))
                }
                isX86 = exitValue == 1

                val archType = if (isX86) "x86" else "x64"
                logger.println(LuaBundle.message("run.attach.launch_info", program, archType), ConsoleViewContentType.SYSTEM_OUTPUT)
                // attach debugger
                val exe = LuaFileUtil.getPluginVirtualFile(String.format("debugger/windows/%s/Debugger.exe", archType))

                val commandLine = GeneralCommandLine(exe!!)
                commandLine.charset = Charset.forName("UTF-8")
                commandLine.addParameters("-m", "run", "-c", program, "-e", emmyLua, "-w", workingDir)
                if (args != null) {
                    val argString = args.joinToString(" ")
                    if (!argString.isEmpty()) {
                        commandLine.addParameters("-a", argString)
                    }
                }

                handler = OSProcessHandler(commandLine)
                handler!!.addProcessListener(processListener)
                handler!!.startNotify()

                //writer = BufferedWriter(OutputStreamWriter(handler!!.process.outputStream))
            }
        } catch (e: Exception) {
            logger.error(e.message!!)
            session.stop()
        }

    }

    private fun procPack() {
        try {
            val reader = DataInputStream(socket.getInputStream())
            while (true) {
                val len = reader.readInt()
                val bytes = reader.readBytes(len)
                handleMsg(bytes)
            }
        } catch (e: Exception) {
            println("----------> " + e.message)
        }
    }

    private fun handleMsg(byteArray: ByteArray) {
        println(byteArray)
    }

    private fun receive(data: ByteArray, size: Int) {
        receiveBuffer.write(data)

    }

    internal fun stop(detach: Boolean = true) {
        if (detach)
            send("detach")
        writer = null
        if (handler != null) {
            handler!!.destroyProcess()
            handler = null
        }
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

        /*with(writer!!) {
            writeInt(byteArray.size)
            write(byteArray)
            flush()
        }*/
    }

    internal fun send(data: String) {
        if (writer != null) {
            /*try {
                writer!!.write(data)
                writer!!.write("\n")
                writer!!.flush()
            } catch (e: IOException) {
                writer = null
                session.stop()
            }*/

        }
    }

    private fun parse(dataMsg: String): LuaAttachProto? {
        var data = dataMsg
        val builderFactory = DocumentBuilderFactory.newInstance()
        try {
            val documentBuilder = builderFactory.newDocumentBuilder()
            data = "<data>$data</data>"
            val document = documentBuilder.parse(ByteArrayInputStream(data.toByteArray(charset("UTF-8"))))
            val root = document.documentElement
            val childNodes = root.childNodes
            for (i in 0 until childNodes.length) {
                val item = childNodes.item(i)
                if (item.nodeName == "type") {
                    val type = Integer.parseInt(item.textContent)
                    return createProto(type, root)
                }
            }
        } catch (e: Exception) {
            println("Parse exception:")
            println(data)
        }

        return null
    }

    @Throws(Exception::class)
    private fun createProto(type: Int, root: Element): LuaAttachProto {
        val proto = protoFactory!!.createProto(type)
        proto.doParse(root)
        return proto
    }

    fun eval(expr: String, stack: Int, depth: Int, callback: EvalCallback) {
        val id = evalIdCounter++
        val info = EvalInfo()
        info.callback = callback
        info.expr = expr
        callbackMap.put(id, info)
        send(String.format("eval %d %d %d %s", id, stack, depth, expr))
    }

    internal fun addBreakpoint(index: Int, breakpoint: XLineBreakpoint<*>) {
        val expression = breakpoint.conditionExpression
        val exp = expression?.expression ?: ""
        send(String.format("setb %d %d %s", index, breakpoint.line, exp))
    }

    internal fun removeBreakpoint(index: Int, breakpoint: XLineBreakpoint<*>) {
        send(String.format("delb %d %d", index, breakpoint.line))
    }

    internal fun sendDone() {
        send("done")
    }

    internal fun sendRun() {
        send("run")
    }
}