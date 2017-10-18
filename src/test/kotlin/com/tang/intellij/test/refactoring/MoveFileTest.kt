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

package com.tang.intellij.test.refactoring

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.refactoring.MultiFileTestCase
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor

class MoveFileTest : MultiFileTestCase() {
    override fun getTestRoot() = "/refactoring/"

    override fun getTestDataPath() = "src/test/resources/"

    fun testMoveFile() {
        val fileToMove = "A.lua"
        val targetDirName = "to"
        doTest { rootDir, _ ->
            val child = rootDir.findFileByRelativePath(fileToMove)
            assertNotNull("File $fileToMove not found", child)
            val file = myPsiManager.findFile(child!!)!!

            val child1 = rootDir.findChild(targetDirName)
            assertNotNull("File $targetDirName not found", child1)
            val targetDirectory = myPsiManager.findDirectory(child1!!)

            MoveFilesOrDirectoriesProcessor(myProject, arrayOf<PsiElement>(file), targetDirectory,
                    false, false, null, null).run()

            FileDocumentManager.getInstance().saveAllDocuments()
        }
    }
}