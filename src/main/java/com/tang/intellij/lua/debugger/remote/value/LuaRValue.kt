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

package com.tang.intellij.lua.debugger.remote.value

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.psi.PsiManager
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.frame.XNamedValue
import com.intellij.xdebugger.frame.XNavigatable
import com.intellij.xdebugger.impl.XSourcePositionImpl
import com.tang.intellij.lua.psi.LuaDeclarationTree
import org.luaj.vm2.LuaValue

/**
 * remote value
 * Created by tangzx on 2017/4/16.
 */
abstract class LuaRValue(name: String) : XNamedValue(name) {

    protected lateinit var session: XDebugSession

    protected abstract fun parse(data: LuaValue, desc: String)

    var parent: LuaRValue? = null

    override fun computeSourcePosition(xNavigable: XNavigatable) {
        computeSourcePosition(xNavigable, name, session)
    }

    companion object {

        fun create(name: String, data: LuaValue, desc: String, session: XDebugSession): LuaRValue {
            var describe = desc
            val value: LuaRValue = when {
                data.istable() -> LuaRTable(name)
                data.isfunction() -> LuaRFunction(name)
                data.isnil() -> {
                    describe = "nil"
                    LuaRPrimitive(name)
                }
                else -> LuaRPrimitive(name)
            }

            value.session = session
            value.parse(data, describe)
            return value
        }

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
                    val element = psiFile.findElementAt(lineEndOffset) ?: return
                    LuaDeclarationTree.get(psiFile).walkUpLocal(element) {
                        if (name == it.name) {
                            val position = XSourcePositionImpl.createByElement(it.psi)
                            xNavigable.setSourcePosition(position)
                            return@walkUpLocal false
                        }
                        true
                    }
                }
            }
        }
    }
}
