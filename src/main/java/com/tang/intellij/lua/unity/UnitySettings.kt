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

package com.tang.intellij.lua.unity

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

interface UnitySettingsListener {
    fun onUnitySettingsChanged()
}

@State(name = "UnitySettings", storages = [Storage("emmy.xml")])
class UnitySettings : PersistentStateComponent<UnitySettings> {
    companion object {
        val TOPIC = Topic.create("Unity settings changed.", UnitySettingsListener::class.java)

        @JvmStatic fun getInstance(): UnitySettings {
            return ServiceManager.getService(UnitySettings::class.java)
        }
    }

    var port = 996

    override fun getState(): UnitySettings? {
        return this
    }

    override fun loadState(settings: UnitySettings) {
        XmlSerializerUtil.copyBean(settings, this)
    }

    fun fireChanged() {
        ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC).onUnitySettingsChanged()
    }
}