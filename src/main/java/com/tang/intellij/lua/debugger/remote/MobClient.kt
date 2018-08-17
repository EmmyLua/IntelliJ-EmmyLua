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

package com.tang.intellij.lua.debugger.remote

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.tang.intellij.lua.debugger.LogConsoleType
import com.tang.intellij.lua.debugger.remote.commands.DebugCommand
import com.tang.intellij.lua.debugger.remote.commands.DefaultCommand
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern

class MobClient(private val socketChannel: SocketChannel, private val listener: MobServerListener) {

    private var isStopped: Boolean = false
    private val commands = LinkedList<DebugCommand>()
    private var currentCommandWaitForResp: DebugCommand? = null
    private var streamWriter: OutputStreamWriter? = null
    private val socket = socketChannel.socket()
    private val receiveBufferSize = 1024 * 1024

    init {
        socket.receiveBufferSize = receiveBufferSize
        ApplicationManager.getApplication().executeOnPooledThread {
            doReceive()
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            doSend()
        }
    }

    private fun doSend() {
        try {
            streamWriter = OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"))

            while (socket.isConnected) {
                if (isStopped) break

                var command: DebugCommand
                while (commands.size > 0 && currentCommandWaitForResp == null) {
                    if (currentCommandWaitForResp == null) {
                        command = commands.poll()
                        command.debugProcess = listener.process
                        command.write(this)
                        streamWriter!!.write("\n")
                        streamWriter!!.flush()
                        if (command.getRequireRespLines() > 0)
                            currentCommandWaitForResp = command
                    }
                }
                Thread.sleep(5)
            }
        } catch (e: SocketException) {
            //e.message?.let { listener.error(it) }
        } catch (e: Exception) {
            e.message?.let { listener.error(it) }
        } finally {
            listener.println("Disconnected.", LogConsoleType.NORMAL, ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    private fun doReceive() {
        try {
            var readSize: Int
            val bf = ByteBuffer.allocate(receiveBufferSize)
            while (!isStopped) {
                readSize = socketChannel.read(bf)
                if (readSize > 0) {
                    var begin = 0
                    for (i in 1..readSize + 1) {
                        if (bf[i - 1].toInt() == '\n'.toInt()) {
                            onResp(String(bf.array(), begin, i))
                            begin = i
                        }
                    }
                    if (begin < readSize) {
                        onResp(String(bf.array(), begin, readSize))
                    }
                    bf.clear()
                }
            }
        } catch (e: IOException) {
            onSocketClosed()
        } catch (e: Exception) {
            e.message?.let { listener.error(it) }
        }
    }

    private fun onResp(data: String) {
        val cmd = currentCommandWaitForResp
        if (cmd != null) {
            val eat = cmd.handle(data)
            if (eat > 0) {
                if (cmd.isFinished())
                    currentCommandWaitForResp = null
                return
            }
        }

        val pattern = Pattern.compile("(\\d+) (\\w+)( (.+))?")
        val matcher = pattern.matcher(data)
        if (matcher.find()) {
            val code = Integer.parseInt(matcher.group(1))
            //String status = matcher.group(2);
            val context = matcher.group(4)
            listener.handleResp(this, code, context)
        }
    }

    private fun onSocketClosed() {
        listener.onDisconnect(this)
    }

    @Throws(IOException::class)
    fun write(data: String) {
        streamWriter!!.write(data)
        //println("send:" + data)
    }

    fun stop() {
        try {
            streamWriter?.write("done\n")
        } catch (ignored: IOException) {
        }

        isStopped = true
        currentCommandWaitForResp = null
        try {
            socket.close()
        } catch (ignored: Exception) {
        }
    }

    fun sendAddBreakpoint(file: String, line: Int) {
        addCommand("SETB $file $line")
    }

    fun sendRemoveBreakpoint(file: String, line: Int) {
        addCommand("DELB $file $line")
    }

    fun addCommand(command: String, rl: Int = 1) {
        addCommand(DefaultCommand(command, rl))
    }

    fun addCommand(command: DebugCommand) {
        commands.add(command)
    }
}