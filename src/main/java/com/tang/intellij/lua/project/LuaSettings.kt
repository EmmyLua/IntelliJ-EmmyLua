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

package com.tang.intellij.lua.project

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.tang.intellij.lua.lang.LuaLanguageLevel
import java.nio.charset.Charset

/**
 *
 * Created by Administrator on 2017/6/12.
 */
@State(name = "LuaSettings", storages = [(Storage("emmy.xml"))])
class LuaSettings : PersistentStateComponent<LuaSettings> {
    var constructorNames: Array<String> = arrayOf("new", "get")

    //Doc文档严格模式，对不合法的注解报错
    var isStrictDoc: Boolean = false

    //在未匹配end的statement后回车会自动补全
    var isSmartCloseEnd: Boolean = true

    //在代码完成时使用参数完成模板
    var autoInsertParameters: Boolean = false

    var isShowWordsInFile: Boolean = true

    // Throw errors if specified and found types do not match
    var isEnforceTypeSafety: Boolean = false

    var isNilStrict: Boolean = false

    var isRecognizeGlobalNameAsType = true

    var additionalSourcesRoot = arrayOf<String>()

    /**
     * 使用泛型
     */
    var enableGeneric: Boolean = false

    /**
     * (KB)
     */
    var tooLargerFileThreshold = 1024

    var attachDebugDefaultCharsetName = "UTF-8"

    var attachDebugCaptureStd = true

    var attachDebugCaptureOutput = true

    /**
     * Lua language level
     */
    var languageLevel = LuaLanguageLevel.LUA53

    override fun getState(): LuaSettings? {
        return this
    }

    override fun loadState(luaSettings: LuaSettings) {
        XmlSerializerUtil.copyBean(luaSettings, this)
    }

    var constructorNamesString: String
        get() {
            return constructorNames.joinToString(";")
        }
        set(value) {
            constructorNames = value.split(";").map { it.trim() }.toTypedArray()
        }

    val attachDebugDefaultCharset: Charset get() {
        return Charset.forName(attachDebugDefaultCharsetName) ?: Charset.forName("UTF-8")
    }

    companion object {

        val instance: LuaSettings
            get() = ServiceManager.getService(LuaSettings::class.java)

        fun isConstructorName(name: String): Boolean {
            return instance.constructorNames.contains(name.toLowerCase())
        }
    }
}
