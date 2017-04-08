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

package com.tang.intellij.lua.lang.type;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 类型集合
 * Created by TangZX on 2016/12/4.
 */
public class LuaTypeSet {

    public static LuaTypeSet create() {
        return new LuaTypeSet();
    }

    public static LuaTypeSet create(LuaDocClassDef ... classDefs) {
        LuaTypeSet set = new LuaTypeSet();
        for (LuaDocClassDef def : classDefs) {
            set.types.add(def.getClassType());
        }
        return set;
    }

    public static LuaTypeSet create(LuaType ... types) {
        LuaTypeSet set = new LuaTypeSet();
        Collections.addAll(set.types, types);
        return set;
    }

    private List<LuaType> types = new ArrayList<>();

    public List<LuaType> getTypes() {
        return types;
    }

    @Nullable
    public LuaType getFirst() {
        if (isEmpty())
            return null;
        return types.get(0);
    }

    public boolean isEmpty() {
        return types.isEmpty();
    }

    public void addType(LuaType type) {
        types.add(type);
    }

    public static void serialize(LuaTypeSet set, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeInt(set.types.size());
        for (int i = 0; i < set.types.size(); i++) {
            LuaType type = set.types.get(i);
            type.serialize(stubOutputStream);
        }
    }

    public static LuaTypeSet deserialize(@NotNull StubInputStream stubInputStream) throws IOException {
        LuaTypeSet set = LuaTypeSet.create();
        int num = stubInputStream.readInt();
        for (int i = 0; i < num; i++) {
            LuaType type = new LuaType();
            type.deserialize(stubInputStream);
            set.addType(type);
        }
        return set;
    }
}
