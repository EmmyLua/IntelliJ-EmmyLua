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

package com.tang.intellij.lua.debugger.unity

import com.intellij.execution.process.OSProcessUtil
import com.intellij.execution.process.ProcessInfo
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

enum class GetProcessOptions(val v:Int) {
    Editor(0x1),
    Players(0x2),
    All(Editor.v or Players.v);

    fun hasFlag(flag: GetProcessOptions): Boolean{
        return this.v and flag.v == flag.v
    }
}
/**
 *
 * Created by Taigacon on 2018/11/6.
 */
class UnityProcessDiscovery {
    companion object {
        //private val mutex = ReentrantLock()

        //private val unityProcessess = mutableListOf<ProcessInfo>()
        //private var unityProcessesFinished  = true

        fun getAttachableProcesses(flags: GetProcessOptions): List<ProcessInfo> {
            val processes = mutableListOf<ProcessInfo>()
            if (flags.hasFlag(GetProcessOptions.Editor)){
                processes.addAll(getUnityEditorProcesses())
            }
            //if (flags.hasFlag(GetProcessOptions.Players)){
            //    processes.addAll(getUnityPlayerProcesses(true))
            //}
            return processes
        }

        private fun getUnityEditorProcesses(): List<ProcessInfo> {
            val list = arrayListOf<ProcessInfo>()
            val systemProcesses = OSProcessUtil.getProcessList()
            for (processInfo in systemProcesses){
                if(processInfo.executableDisplayName == "Unity"){
                    list.add(processInfo)
                }
            }
            return list
        }
    }

    class ConnectorRegistry {
        private val processIdLock = ReentrantLock()
        private var nextProcessId = 1000000
        private val processIdToUniqueId = mutableMapOf<Int, String>()
        private val uniqueIdToProcessId  = mutableMapOf<String, Int>()
        //val connectors = mutableMapOf<Int, >()

        fun getProcessIdForUniqueId(uid: String): Int{
            processIdLock.withLock {
                val processId = uniqueIdToProcessId[uid]
                if(processId != null){
                    return processId
                }
                val newProcessId = nextProcessId++
                processIdToUniqueId.set(newProcessId, uid)
                uniqueIdToProcessId.set(uid, newProcessId)
                return newProcessId
            }
        }

        fun getUniqueIdFromProcessId(processId: Int): String?{
            processIdLock.withLock {
                return processIdToUniqueId[processId]
            }
        }
    }
}
