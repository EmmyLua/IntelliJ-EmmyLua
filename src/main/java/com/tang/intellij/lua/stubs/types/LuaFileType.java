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

package com.tang.intellij.lua.stubs.types;

import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IStubFileElementType;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaFile;
import com.tang.intellij.lua.stubs.LuaFileStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/11/27.
 */
public class LuaFileType extends IStubFileElementType<LuaFileStub> {
    public LuaFileType() {
        super(LuaLanguage.INSTANCE);
    }

    @Override
    public StubBuilder getBuilder() {
        return new DefaultStubBuilder(){
            @NotNull
            @Override
            protected StubElement createStubForFile(@NotNull PsiFile file) {
                if (file instanceof LuaFile)
                    return new LuaFileStub((LuaFile) file);
                return super.createStubForFile(file);
            }

            /*@Override
            public boolean skipChildProcessingWhenBuildingStubs(@NotNull ASTNode parent, @NotNull ASTNode node) {
                IElementType type = node.getElementType();
                return type == LuaTypes.BLOCK;
            }*/
        };
    }

    @Override
    public void serialize(@NotNull LuaFileStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        LuaTypeSet returnedType = stub.getReturnedType();
        LuaTypeSet.serialize(returnedType, dataStream);
    }

    @NotNull
    @Override
    public LuaFileStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        LuaTypeSet typeSet = LuaTypeSet.deserialize(dataStream);
        return new LuaFileStub(null, typeSet);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.file";
    }
}
