package com.tang.intellij.lua.editor.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.lang.type.LuaString
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

class AliasEnumCompletionProvider : LuaCompletionProvider() {
    override fun addCompletions(session: CompletionSession) {
        val completionParameters = session.parameters
        val completionResultSet = session.resultSet
        val file = completionParameters.originalFile
        val cur = file.findElementAt(completionParameters.offset - 1)
        if (cur != null) {
            val stringLiteral = cur.parent
            if (stringLiteral is LuaExpr) {
                val ty = stringLiteral.shouldBe(SearchContext.get(stringLiteral.project))
                ty.each {
                    if (it is TyStringLiteral) {
                        val lookupElement = LookupElementBuilder.create(it.content)
                            .withIcon(LuaIcons.STRING_LITERAL)
                            .withInsertHandler(StringEnumInsertHandler())
                        completionResultSet.addElement(
                            PrioritizedLookupElement.withPriority(
                                lookupElement,
                                111111999.0
                            )
                        )
                    }
                }
                completionResultSet.stopHere()
            }
        }
    }

    internal class StringEnumInsertHandler : InsertHandler<LookupElement> {
        private fun trimQuote(s: String): String {
            if (s.startsWith('\'')) {
                return s.trim('\'')
            } else if (s.startsWith('\"')) {
                return s.trim('\"')
            }
            return s
        }

        override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
            val tailOffset = insertionContext.tailOffset
            val cur = insertionContext.file.findElementAt(tailOffset)

            if (cur != null) {
                val start = cur.textOffset

                val ls = LuaString.getContent(cur.text)
                insertionContext.document.deleteString(start + ls.start, start + ls.end)

                val lookupString = trimQuote(lookupElement.lookupString)
                insertionContext.document.insertString(start + ls.start, lookupString)
                insertionContext.editor.caretModel.moveToOffset(start + ls.start + lookupString.length)
            }
        }
    }
}