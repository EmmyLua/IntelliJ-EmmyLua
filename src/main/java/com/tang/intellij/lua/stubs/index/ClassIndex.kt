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

class ClassEntry

class ClassIndex : FileBasedIndexExtension<ClassEntry, Int>() {

    val NAME:ID<ClassEntry, Int> = ID.create("lua.file.class")

    val myIndexer = DataIndexer<ClassEntry, Int, FileContent> { fileContent ->
        fileContent.psiFile.accept(object :LuaVisitor() {
            override fun visitComment(comment: PsiComment?) {
                if (comment is LuaComment) {
                    val type = comment.classDef.classType

                }
            }

            override fun visitPsiElement(o: LuaPsiElement) {
                o.acceptChildren(this)
            }
        })
        emptyMap()
    }

    override fun getKeyDescriptor(): KeyDescriptor<ClassEntry> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getName(): ID<ClassEntry, Int> = NAME

    override fun getValueExternalizer(): DataExternalizer<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dependsOnFileContent() = true

    override fun getIndexer(): DataIndexer<ClassEntry, Int, FileContent> = myIndexer

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> file.fileType == LuaFileType.INSTANCE }
    }

    override fun getVersion() = LuaLanguage.INDEX_VERSION
}