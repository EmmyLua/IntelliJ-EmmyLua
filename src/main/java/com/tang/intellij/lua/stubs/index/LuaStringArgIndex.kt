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

package com.tang.intellij.lua.stubs.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.*
import java.io.DataInput
import java.io.DataOutput

class LuaStringArgIndex : FileBasedIndexExtension<String, LuaStringArgIndex.LuaCallOccurrence>(), PsiDependentIndex {

    companion object {
        val NAME: ID<String, LuaCallOccurrence> = ID.create("lua.call.string.param")

        fun processValues(key: String, scope: GlobalSearchScope, processor: Processor<LuaCallArg>) {
            FileBasedIndex.getInstance().processValues(NAME, key, null, { _, v ->
                ContainerUtil.process(v.args, processor)
            }, scope)
        }
    }

    override fun getValueExternalizer() = object : DataExternalizer<LuaCallOccurrence> {
        override fun save(output: DataOutput, occurrence: LuaCallOccurrence) {
            output.writeInt(occurrence.args.size)
            occurrence.args.forEach {
                output.writeInt(it.argIndex)
                output.writeUTF(it.argString)
            }
        }

        override fun read(input: DataInput): LuaCallOccurrence {
            val list = mutableListOf<LuaCallArg>()
            val size = input.readInt()
            for (i in 0 until size) {
                val argIndex = input.readInt()
                val argString = input.readUTF()
                list.add(LuaCallArg(argIndex, argString))
            }
            return LuaCallOccurrence(list)
        }
    }

    override fun getName() = NAME

    override fun getVersion() = LuaLanguage.INDEX_VERSION

    override fun dependsOnFileContent() = true

    override fun getIndexer() = DataIndexer<String, LuaCallOccurrence, FileContent> {
        val map = mutableMapOf<String, LuaCallOccurrence>()
        PsiTreeUtil.findChildrenOfType(it.psiFile, LuaCallExpr::class.java).forEach { call ->
            call.argList.forEach { arg ->
                val name = call.expr.name
                if (name != null && arg is LuaLiteralExpr && arg.kind == LuaLiteralKind.String) {
                    val sv = arg.stringValue
                    if (sv.length > 1) {
                        val occ = map.getOrPut(name) { LuaCallOccurrence(mutableListOf()) }
                        occ.args.add(LuaCallArg(0, sv))
                    }
                }
            }
        }
        map
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return DefaultFileTypeSpecificInputFilter(LuaFileType.INSTANCE)
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    data class LuaCallOccurrence(val args: MutableList<LuaCallArg>)

    data class LuaCallArg(val argIndex: Int, val argString: String)
}