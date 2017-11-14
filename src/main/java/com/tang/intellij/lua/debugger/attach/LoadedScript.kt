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

import com.intellij.openapi.vfs.VirtualFile


enum class CodeState {
    Normal            ,    // The code is normal.
    Unavailable       ,    // The code for the script was not available.
    Binary            ,    // The code was loaded as a binary/compiled file
}

/**
 *
 * Created by tangzx on 2017/4/3.
 */
data class LoadedScript internal constructor(val file: VirtualFile,
                                             val index: Int,
                                             val name: String,
                                             val codeState: CodeState)
