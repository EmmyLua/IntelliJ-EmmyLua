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
import com.intellij.util.TimeoutUtil
import com.intellij.util.io.BaseOutputReader
import com.tang.intellij.lua.debugger.remote.commands.DebugCommand
import com.tang.intellij.lua.debugger.remote.commands.DefaultCommand
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Future
import java.util.regex.Pattern

class MobClient(val socket: Socket, val listener: MobServerListener) {
    inner class LuaDebugReader(inputStream: InputStream, charset: Charset) : BaseOutputReader(inputStream, charset) {
        init {
            start(javaClass.name)
        }

        override fun doRun() {
            try {
                while (true) {
                    val read = readAvailableBlocking()

                    if (!read) {
                        break
                    } else {
                        if (isStopped) {
                            break
                        }

                        TimeoutUtil.sleep(mySleepingPolicy.getTimeToSleep(true).toLong())
                    }
                }
            } catch (e: Exception) {
                //e.printStackTrace()
            } finally {
                onSocketClosed()
            }
        }

        override fun onTextAvailable(s: String) {
            onResp(s)
        }

        override fun executeOnPooledThread(runnable: Runnable): Future<*> {
            return ApplicationManager.getApplication().executeOnPooledThread(runnable)
        }
    }

    private var isStopped: Boolean = false
    private val commands = LinkedList<DebugCommand>()
    private var debugReader: LuaDebugReader? = null
    private var currentCommandWaitForResp: DebugCommand? = null
    private var streamWriter: OutputStreamWriter? = null
    private val stringBuffer = StringBuffer(2048)

    init {
        debugReader = LuaDebugReader(socket.getInputStream(), Charset.forName("UTF-8"))

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                streamWriter = OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"))

                while (socket.isConnected) {
                    if (isStopped) break

                    var command: DebugCommand
                    while (commands.size > 0 && currentCommandWaitForResp == null) {
                        if (currentCommandWaitForResp == null) {
                            command = commands.poll()
                            command.setDebugProcess(listener.process)
                            command.write(this)
                            streamWriter!!.write("\n")
                            streamWriter!!.flush()
                            if (command.requireRespLines > 0)
                                currentCommandWaitForResp = command
                        }
                    }
                    Thread.sleep(5)
                }
            } catch (e: Exception) {
                //e.printStackTrace()
            } finally {
                listener.println("Disconnected.", ConsoleViewContentType.SYSTEM_OUTPUT)
            }
        }
    }

    private fun onResp(data: String) {
        if (currentCommandWaitForResp != null) {
            stringBuffer.append(data)
            val eat = currentCommandWaitForResp!!.handle(stringBuffer.toString())
            if (eat > 0) {
                stringBuffer.delete(0, eat)
                if (currentCommandWaitForResp!!.isFinished)
                    currentCommandWaitForResp = null
            }
        } else {
            stringBuffer.setLength(0)
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
        } catch (ignored: IOException) { }

        isStopped = true
        currentCommandWaitForResp = null
        debugReader?.stop()
        try {
            socket.close()
        } catch (ignored: Exception) {}
    }

    fun sendAddBreakpoint(file: String, line: Int) {
        addCommand("SETB $file $line")
    }

    fun sendRemoveBreakpoint(file: String, line: Int) {
        addCommand("DELB $file $line")
    }

    fun addCommand(command: String) {
        addCommand(DefaultCommand(command, 0))
    }

    fun addCommand(command: DebugCommand) {
        commands.add(command)
    }
}