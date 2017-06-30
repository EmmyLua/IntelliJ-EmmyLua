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

import com.intellij.psi.PsiComment
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.KeyDescriptor
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaPsiElement
import com.tang.intellij.lua.psi.LuaVisitor
import java.io.DataInput
import java.io.DataOutput

data class ClassEntry(val clazz:String, val superClazz:String?)

class ClassIndex : FileBasedIndexExtension<String, ClassEntry>() {

    val NAME:ID<String, ClassEntry> = ID.create("lua.file.class")

    val myIndexer = DataIndexer<String, ClassEntry, FileContent> { fileContent ->
        val map = mutableMapOf<String, ClassEntry>()
        fileContent.psiFile.accept(object :LuaVisitor() {
            override fun visitComment(comment: PsiComment?) {
                if (comment is LuaComment) {
                    val type = comment.classDef?.classType
                    if (type != null) {
                        map.put(type.className, ClassEntry(type.className, type.superClassName))
                    }
                }
            }

            override fun visitPsiElement(o: LuaPsiElement) {
                o.acceptChildren(this)
            }
        })
        map
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getName(): ID<String, ClassEntry> = NAME

    override fun getValueExternalizer(): DataExternalizer<ClassEntry> {
        return object :DataExternalizer<ClassEntry> {
            override fun read(input: DataInput): ClassEntry {
                val clsName = input.readUTF()!!
                val hasSur = input.readBoolean()
                var surName:String? = null
                if (hasSur) {
                    surName = input.readUTF()
                }
                return ClassEntry(clsName, surName)
            }

            override fun save(output: DataOutput, entry: ClassEntry) {
                output.writeUTF(entry.clazz)
                output.writeBoolean(entry.superClazz != null)
                if (entry.superClazz != null) {
                    output.writeUTF(entry.superClazz)
                }
            }
        }
    }

    override fun dependsOnFileContent() = true

    override fun getIndexer(): DataIndexer<String, ClassEntry, FileContent> = myIndexer

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> file.fileType == LuaFileType.INSTANCE }
    }

    override fun getVersion() = LuaLanguage.INDEX_VERSION
}