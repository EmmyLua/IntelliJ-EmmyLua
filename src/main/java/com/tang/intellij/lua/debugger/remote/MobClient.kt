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
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class MobClient(private val socketChannel: SocketChannel, private val listener: MobServerListener) {

    private val isStopped = AtomicBoolean(false)
    private val commands = ConcurrentLinkedQueue<DebugCommand>()
    @Volatile private var currentCommandWaitForResp: DebugCommand? = null
    private var streamWriter: OutputStreamWriter? = null
    private val socket = socketChannel.socket()

    fun start() {
        ApplicationManager.getApplication().executeOnPooledThread { doReceive() }
        ApplicationManager.getApplication().executeOnPooledThread { doSend() }
    }

    private fun doSend() {
        try {
            streamWriter = OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8)
            while (!isStopped.get()) {
                if (currentCommandWaitForResp == null) {
                    val command = commands.poll()
                    if (command != null) {
                        command.debugProcess = listener.process
                        if (command.getRequireRespLines() > 0)
                            currentCommandWaitForResp = command
                        command.write(this)
                        streamWriter!!.write("\n")
                        streamWriter!!.flush()
                    }
                }
                Thread.sleep(5)
            }
        } catch (e: Exception) {
            onClosed("send failed", e)
        }
    }

    private fun doReceive() {
        try {
            val input = socket.getInputStream().buffered()
            while (!isStopped.get()) {
                val line = ByteArrayOutputStream()
                while (true) {
                    val byte = input.read()
                    if (byte == -1) throw EOFException()
                    if (byte == '\n'.code) break
                    line.write(byte)
                }
                val header = line.toString(Charsets.UTF_8.name()).trimEnd('\r')
                val code = header.substringBefore(' ').toIntOrNull() ?: continue
                val length = when {
                    header.matches(Regex("200 OK \\d+")) -> header.substringAfterLast(' ').toInt()
                    code == 401 || code == 204 -> header.substringAfterLast(' ').toInt()
                    else -> null
                }
                val body = length?.let {
                    val bytes = ByteArray(it)
                    var offset = 0
                    while (offset < bytes.size) {
                        val count = input.read(bytes, offset, bytes.size - offset)
                        if (count == -1) throw EOFException()
                        offset += count
                    }
                    String(bytes, Charsets.UTF_8)
                }
                if (code in 201..204) {
                    listener.handleResp(this, code, body ?: header.split(' ', limit = 3).getOrNull(2))
                    continue
                }
                val command = currentCommandWaitForResp
                if (command != null) {
                    command.handle("$header\n")
                    if (body != null) command.handle(body)
                    if (command.isFinished()) currentCommandWaitForResp = null
                } else {
                    listener.handleResp(this, code, body ?: header.split(' ', limit = 3).getOrNull(2))
                }
            }
        } catch (e: EOFException) {
            onClosed("EOF", e)
        } catch (e: Exception) {
            onClosed("receive failed", e)
        }
    }

    @Throws(IOException::class)
    fun write(data: String) {
        streamWriter!!.write(data)
    }

    fun stop() {
        onClosed("stopped")
    }

    private fun onClosed(reason: String, cause: Exception? = null) {
        if (!isStopped.compareAndSet(false, true)) return
        currentCommandWaitForResp = null
        commands.clear()
        try {
            socket.close()
        } catch (ignored: IOException) {
        }
        val detail = cause?.let { " (${it.javaClass.simpleName})" } ?: ""
        listener.println("Disconnected: $reason$detail.", LogConsoleType.NORMAL, ConsoleViewContentType.SYSTEM_OUTPUT)
        listener.onDisconnect(this)
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