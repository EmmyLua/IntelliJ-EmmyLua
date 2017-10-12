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

package com.tang.intellij.lua.stubs

import com.intellij.openapi.util.Ref
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IStubFileElementType
import com.tang.intellij.lua.lang.LuaParserDefinition
import com.tang.intellij.lua.psi.LuaFile
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.types.LuaFileType
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

/**

 * Created by tangzx on 2016/11/27.
 */
class LuaFileStub : PsiFileStubImpl<LuaFile> {
    private var retTypeRef: Ref<ITy>? = null
    private var file: LuaFile? = null
    private var moduleName:String? = null

    constructor(file: LuaFile) : super(file) {
        this.file = file
        moduleName = file.findModuleName()
    }

    constructor(file: LuaFile?, module:String?, type: ITy) : super(file) {
        this.file = file
        moduleName = module
        retTypeRef = Ref.create(type)
    }

    val module: String? get() {
        return moduleName
    }

    override fun getType(): LuaFileType = LuaParserDefinition.FILE

    fun getReturnedType(context: SearchContext): ITy {
        if (retTypeRef == null && file != null) {
            val returnedType = file!!.guessReturnedType(context)
            retTypeRef = Ref.create(returnedType)
        }
        return retTypeRef?.get() ?: Ty.UNKNOWN
    }
}