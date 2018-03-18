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
import com.tang.intellij.lua.debugger.DebugLogger
import com.tang.intellij.lua.debugger.LogConsoleType
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

interface MobServerListener : DebugLogger {
    fun onConnect(client: MobClient)
    fun handleResp(client: MobClient, code: Int, data: String?)
    fun onDisconnect(client: MobClient)
    val process: LuaMobDebugProcess
}

class MobServer(private val listener: MobServerListener) : Runnable {
    private var server: ServerSocketChannel? = null
    private var client: MobClient? = null
    private var port: Int = 0
    private var isStopped: Boolean = false

    @Throws(IOException::class)
    fun start(port: Int) {
        this.port = port
        if (server == null)
            server = ServerSocketChannel.open()
        server?.bind(InetSocketAddress(port))
        val thread = Thread(this)
        thread.start()
    }

    fun restart() {
        client?.stop()
        client = null
    }

    override fun run() {
        while (!isStopped) {
            try {
                val accept = server!!.accept()
                if (client != null) {
                    try {
                        accept.close()
                    } catch (e: Exception) {}
                } else {
                    listener.println("Connected.", LogConsoleType.NORMAL, ConsoleViewContentType.SYSTEM_OUTPUT)
                    client = MobClient(accept, listener)
                    listener.onConnect(client!!)
                }
            } catch (e: IOException) {
                break
            }
        }
    }

    fun stop() {
        isStopped = true
        client?.stop()
        client = null
        try {
            server?.close()
        } catch (ignored: IOException) {

        }
    }
}
