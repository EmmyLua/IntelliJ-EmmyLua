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

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.lang.LuaParserDefinition
import com.tang.intellij.lua.psi.LuaPsiFile
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

/**

 * Created by tangzx on 2016/11/27.
 */
class LuaFileElementType : IStubFileElementType<LuaFileStub>(LuaLanguage.INSTANCE) {

    companion object {
        val LOG = Logger.getInstance(LuaFileElementType::class.java)
    }

    // debug performance
    override fun parseContents(chameleon: ASTNode): ASTNode? {
        val t = System.currentTimeMillis()
        val contents = super.parseContents(chameleon)
        if (LOG.isDebugEnabled) {
            val dt = System.currentTimeMillis() - t
            val psi = chameleon.psi
            if (psi is LuaPsiFile) {
                val fileName = psi.name
                println("$fileName : $dt")
                LOG.debug("$fileName : $dt")
            }
        }
        return contents
    }

    override fun getBuilder(): StubBuilder {
        return object : DefaultStubBuilder() {
            override fun createStubForFile(file: PsiFile): StubElement<*> {
                if (file is LuaPsiFile)
                    return LuaFileStub(file)
                return super.createStubForFile(file)
            }
        }
    }

    override fun serialize(stub: LuaFileStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.module)
        val returnedType = stub.getReturnedType(SearchContext(stub.project))
        Ty.serialize(returnedType, dataStream)
        if (LOG.isTraceEnabled) {
            println("--------- START: ${stub.psi.name}")
            println(stub.printTree())
            println("--------- END: ${stub.psi.name}")
        }
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): LuaFileStub {
        val moduleRef = dataStream.readName()
        val type = Ty.deserialize(dataStream)
        return LuaFileStub(null, StringRef.toString(moduleRef), type)
    }

    override fun getExternalId() = "lua.file"
}

class LuaFileStub : PsiFileStubImpl<LuaPsiFile> {
    private var retTypeRef: Ref<ITy>? = null
    private var file: LuaPsiFile? = null
    private var moduleName:String? = null

    constructor(file: LuaPsiFile) : super(file) {
        this.file = file
        moduleName = file.findModuleName()
    }

    constructor(file: LuaPsiFile?, module:String?, type: ITy) : super(file) {
        this.file = file
        moduleName = module
        retTypeRef = Ref.create(type)
    }

    val module: String? get() {
        return moduleName
    }

    override fun getType(): LuaFileElementType = LuaParserDefinition.FILE

    fun getReturnedType(context: SearchContext): ITy {
        if (retTypeRef == null && file != null) {
            val returnedType = file!!.guessReturnedType(context)
            retTypeRef = Ref.create(returnedType)
        }
        return retTypeRef?.get() ?: Ty.UNKNOWN
    }
}