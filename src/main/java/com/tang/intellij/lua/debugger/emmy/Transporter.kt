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

package com.tang.intellij.lua.debugger.emmy

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.SystemInfoRt
import com.tang.intellij.lua.debugger.DebugLogger
import com.tang.intellij.lua.debugger.LogConsoleType
import org.scalasbt.ipcsocket.UnixDomainServerSocket
import org.scalasbt.ipcsocket.UnixDomainSocket
import org.scalasbt.ipcsocket.Win32NamedPipeServerSocket
import org.scalasbt.ipcsocket.Win32NamedPipeSocket
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.LinkedBlockingQueue

class StopSign : Message(MessageCMD.Unknown)

interface ITransportHandler {
    fun onReceiveMessage(cmd: MessageCMD, json: String)
    fun onDisconnect()
    fun onConnect(suc: Boolean)
}

abstract class Transporter {

    var handler: ITransportHandler? = null

    var logger: DebugLogger? = null

    protected val messageQueue = LinkedBlockingQueue<IMessage>()

    protected var stopped = false

    open fun start() {

    }

    open fun close() {
        stopped = true
    }

    fun send(msg: IMessage) {
        messageQueue.put(msg)
    }

    protected fun onConnect(suc: Boolean) {
        handler?.onConnect(suc)
        if (suc) {
            logger?.println("Connected.", LogConsoleType.NORMAL, ConsoleViewContentType.LOG_INFO_OUTPUT)
        }
    }

    protected open fun onDisconnect() {
        logger?.println("Disconnected.", LogConsoleType.NORMAL, ConsoleViewContentType.LOG_INFO_OUTPUT)
    }

    protected fun onReceiveMessage(type: MessageCMD, json: String) {
        try {
            handler?.onReceiveMessage(type, json)
        } catch (e: Exception) {
            println(e)
        }
    }
}

abstract class SocketChannelTransporter : Transporter() {

    protected var socket: SocketChannel? = null

    protected fun run() {
        ApplicationManager.getApplication().executeOnPooledThread {
            doReceive()
        }
        ApplicationManager.getApplication().executeOnPooledThread {
            doSend()
        }
    }

    protected open fun getInputStream(): InputStream? {
        return socket?.socket()?.getInputStream()
    }

    protected open fun write(ba: ByteArray) {
        socket?.write(ByteBuffer.wrap(ba))
    }

    private fun doReceive() {
        val iss = getInputStream() ?: return
        val reader = BufferedReader(InputStreamReader(iss))
        while (true) {
            try {
                val cmdValue = reader.readLine()
                val cmd = cmdValue.toInt()
                val json = reader.readLine()
                val type = MessageCMD.values().find { it.ordinal == cmd }
                onReceiveMessage(type ?: MessageCMD.Unknown, json)
            } catch (e: Exception) {
                onDisconnect()
                break
            }
        }
        println(">>> stop receive")
    }

    private fun doSend() {
        while(true) {
            val msg = messageQueue.take()
            if (msg is StopSign)
                break
            try {
                val json = msg.toJSON()
                write("${msg.cmd}\n$json\n".toByteArray())
            } catch (e: IOException) {
                break
            }
        }
        println(">>> stop send")
    }

    override fun close() {
        super.close()
        socket?.close()
        socket = null
    }

    override fun onDisconnect() {
        super.onDisconnect()
        socket = null
    }
}

class SocketClientTransporter(val host: String, val port: Int) : SocketChannelTransporter() {

    private var server: SocketChannel? = null

    override fun start() {
        logger?.println("Try connect $host:$port ...", LogConsoleType.NORMAL, ConsoleViewContentType.LOG_INFO_OUTPUT)
        val server = SocketChannel.open()
        val address = InetAddress.getByName(host)
        var connected = false
        if (server.connect(InetSocketAddress(address,port))) {
            this.server = server
            this.socket = server
            run()
            connected = true
        }
        onConnect(connected)
    }

    override fun onDisconnect() {
        super.onDisconnect()
        handler?.onDisconnect()
    }
}

class SocketServerTransporter(val host: String, val port: Int) : SocketChannelTransporter() {
    private var server = ServerSocketChannel.open()

    override fun start() {
        server.bind(InetSocketAddress(InetAddress.getByName(host), port))
        logger?.println("Server($host:$port) open successfully, wait for connection...", LogConsoleType.NORMAL, ConsoleViewContentType.LOG_INFO_OUTPUT)
        ApplicationManager.getApplication().executeOnPooledThread {
            while (!stopped) {
                val channel = server.accept()
                if (socket != null) {
                    try {
                        channel.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    socket = channel
                    run()
                    onConnect(true)
                }
            }
        }
    }

    override fun close() {
        super.close()
        server.close()
    }
}

private fun getPipename(name: String): String {
    return if (SystemInfoRt.isWindows) {
        """\\.\pipe\emmylua-$name"""
    } else {
        val tmpdir = System.getProperty("java.io.tmpdir")
        "$tmpdir$name"
    }
}

class PipelineClientTransporter(val name: String) : SocketChannelTransporter() {
    private var pipe: Socket? = null

    override fun start() {
        pipe = if (SystemInfoRt.isWindows) {
            Win32NamedPipeSocket(getPipename(name))
        } else {
            UnixDomainSocket(getPipename(name))
        }
        logger?.println("Pipeline($name) connect successfully.", LogConsoleType.NORMAL, ConsoleViewContentType.LOG_INFO_OUTPUT)
        run()
        onConnect(true)
    }

    override fun getInputStream(): InputStream? {
        return pipe?.getInputStream()
    }

    override fun write(ba: ByteArray) {
        pipe?.getOutputStream()?.write(ba)
    }

    override fun close() {
        super.close()
        pipe?.close()
    }

    override fun onDisconnect() {
        super.onDisconnect()
        handler?.onDisconnect()
    }
}

class PipelineServerTransporter(val name: String) : SocketChannelTransporter() {
    private var pipe: ServerSocket? = null
    private var client: Socket? = null

    override fun start() {
        val pipeName = getPipename(name)
        pipe = if (SystemInfoRt.isWindows) {
            Win32NamedPipeServerSocket(pipeName)
        } else {
            val f = File(pipeName)
            if (f.exists()) {
                f.delete()
            }
            UnixDomainServerSocket(pipeName)
        }
        logger?.println("Pipeline($name) open successfully, wait for connection...", LogConsoleType.NORMAL, ConsoleViewContentType.LOG_INFO_OUTPUT)
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                client = pipe?.accept()
                run()
                onConnect(true)
            } catch (e: Exception) {
                println(e)
                logger?.println(e.message ?: "Unknown error.", LogConsoleType.NORMAL, ConsoleViewContentType.LOG_ERROR_OUTPUT)
            }
        }
    }

    override fun getInputStream(): InputStream? {
        return client?.getInputStream()
    }

    override fun write(ba: ByteArray) {
        client?.getOutputStream()?.write(ba)
    }

    override fun close() {
        super.close()
        client?.close()
        client = null
        pipe?.close()
        pipe = null
    }
}