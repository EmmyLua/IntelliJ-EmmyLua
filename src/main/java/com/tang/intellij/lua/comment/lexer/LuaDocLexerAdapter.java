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

package com.tang.intellij.lua.comment.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.tang.intellij.lua.comment.psi.LuaDocTypes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LuaDocLexerAdapter extends MergingLexerAdapter {
    public LuaDocLexerAdapter() {
        super(new DashStripperLexer(new _LuaDocLexer()), TokenSet.create(LuaDocTypes.STRING));
    }

    private static class DashStripperLexer extends LexerBase {

        private _LuaDocLexer myFlexLexer;
        private CharSequence myBuffer;
        private int myBufferIndex;
        private int myBufferEndOffset;
        private IElementType myTokenType;
        private int myTokenEndOffset;
        private int myState;

        DashStripperLexer(_LuaDocLexer luaDocLexer) {
            myFlexLexer = luaDocLexer;
        }

        @Override
        public final void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
            myBuffer = buffer;
            myBufferIndex = startOffset;
            myBufferEndOffset = endOffset;
            myTokenType = null;
            myTokenEndOffset = startOffset;
            myFlexLexer.reset(myBuffer, startOffset, endOffset, initialState);
        }

        @Override
        public int getState() {
            return getTokenStart() == 0 ? 0 : myState;
        }

        @Override
        @NotNull
        public CharSequence getBufferSequence() {
            return myBuffer;
        }

        @Override
        public int getBufferEnd() {
            return myBufferEndOffset;
        }

        @Override
        public final IElementType getTokenType() {
            locateToken();
            return myTokenType;
        }

        @Override
        public final int getTokenStart() {
            locateToken();
            return myBufferIndex;
        }

        @Override
        public final int getTokenEnd() {
            locateToken();
            return myTokenEndOffset;
        }

        @Override
        public final void advance() {
            locateToken();
            myTokenType = null;
        }

        final void locateToken() {
            if (myTokenType != null) return;
            _locateToken();
        }

        private void _locateToken() {
            if (myTokenEndOffset == myBufferEndOffset) {
                myTokenType = null;
                myBufferIndex = myBufferEndOffset;
                return;
            }

            myBufferIndex = myTokenEndOffset;

            flexLocateToken();
        }

        private void flexLocateToken() {
            try {
                myState = myFlexLexer.yystate();
                //myFlexLexer.goTo(myBufferIndex);
                myTokenType = myFlexLexer.advance();
                myTokenEndOffset = myFlexLexer.getTokenEnd();
            } catch (IOException e) {
                // Can't be
            }
        }
    }
}
