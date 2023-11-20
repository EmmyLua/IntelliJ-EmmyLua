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

package com.tang.intellij.lua.psi

import com.intellij.openapi.fileTypes.FileTypeEvent
import com.intellij.openapi.fileTypes.FileTypeListener
import com.intellij.openapi.fileTypes.FileTypeManager
import com.tang.intellij.lua.lang.LuaFileType

class LuaFileManager : FileTypeListener {

    companion object {
        private var myExtensions = mutableListOf<String>()
        private var dirty = true

        val extensions: Array<String> get() {
            if (dirty) {
                dirty = false
                val all = FileTypeManager.getInstance().getAssociations(LuaFileType.INSTANCE).mapNotNull {
                    // *.lua -> .lua
                    // *.lua.txt -> .lua.txt
                    if (it.presentableString.startsWith("*."))
                        it.presentableString.substring(1)
                    else null
                }
                myExtensions.clear()
                myExtensions.addAll(all)
                myExtensions.add("")
            }
            return myExtensions.toTypedArray()
        }
    }

    override fun fileTypesChanged(event: FileTypeEvent) {
        dirty = true
    }
}