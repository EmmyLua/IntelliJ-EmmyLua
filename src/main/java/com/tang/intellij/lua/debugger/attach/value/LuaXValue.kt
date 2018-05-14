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

package com.tang.intellij.lua.debugger.attach.value

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.psi.PsiManager
import com.intellij.util.Processor
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.frame.XNavigatable
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.impl.XSourcePositionImpl
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcessBase
import com.tang.intellij.lua.debugger.attach.readString
import com.tang.intellij.lua.psi.LuaPsiTreeUtilEx
import java.io.DataInputStream

enum class StackNodeId
{
    List,
    Eval,
    StackRoot,

    Table,
    Function,
    UserData,
    String,
    Binary,
    Primitive,

    Error,
}

/**
 *
 * Created by tangzx on 2017/4/2.
 */
abstract class LuaXValue(val L:Long,
                         val process: LuaAttachDebugProcessBase) : XValue() {

    var name: String? = null
    var parent: LuaXValue? = null

    override fun computePresentation(xValueNode: XValueNode, xValuePlace: XValuePlace) {

    }

    open fun toKeyString(): String {
        return toString()
    }

    override fun computeSourcePosition(xNavigable: XNavigatable) {
        if (name != null) {
            computeSourcePosition(xNavigable, name!!, process.session)
        }
    }

    companion object {
        fun computeSourcePosition(xNavigable: XNavigatable, name: String, session: XDebugSession) {
            val currentPosition = session.currentPosition
            if (currentPosition != null) {
                val file = currentPosition.file
                val project = session.project
                val psiFile = PsiManager.getInstance(project).findFile(file)
                val editor = FileEditorManager.getInstance(project).getSelectedEditor(file)

                if (psiFile != null && editor is TextEditor) {
                    val document = editor.editor.document
                    val lineEndOffset = document.getLineStartOffset(currentPosition.line)
                    val element = psiFile.findElementAt(lineEndOffset)
                    LuaPsiTreeUtilEx.walkUpNameDef(element, Processor{ nameDef ->
                        if (name == nameDef.name) {
                            val position = XSourcePositionImpl.createByElement(nameDef)
                            xNavigable.setSourcePosition(position)
                            return@Processor false
                        }
                        true
                    })
                }
            }
        }
    }
}

open class LuaXObjectValue(val id: StackNodeId, L: Long, process: LuaAttachDebugProcessBase)
    : LuaXValue(L, process), IStackNode {

    lateinit var type: String
    lateinit var data: String

    override fun read(stream: DataInputStream) {
        name = stream.readString(process.charset)
        type = stream.readString(process.charset)
        data = stream.readString(process.charset)
    }
}
