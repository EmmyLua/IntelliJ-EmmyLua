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
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
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
    public LuaType getPerfect() {
        for (LuaType type : types) {
            if (!type.isAnonymous())
                return type;
        }
        if (isEmpty())
            return null;
        return types.get(0);
    }

    public LuaTypeSet union(LuaTypeSet other) {
        if (other == null)
            return this;
        LuaTypeSet newSet = new LuaTypeSet();
        newSet.types.addAll(this.types);
        for (LuaType type : other.types) {
            if (!newSet.hasType(type)) {
                newSet.addType(type);
            }
        }
        return newSet;
    }

    public boolean isEmpty() {
        return types.isEmpty();
    }

    public void addType(LuaType type) {
        if (!hasType(type))
            types.add(type);
    }

    private boolean hasType(LuaType type) {
        for (LuaType luaType : types) {
            if (type.equals(luaType))
                return true;
        }
        return false;
    }

    public static void serialize(@Nullable LuaTypeSet set, @NotNull StubOutputStream stubOutputStream) throws IOException {
        boolean notNull = set != null;
        stubOutputStream.writeBoolean(notNull);
        if (notNull) {
            stubOutputStream.writeInt(set.types.size());
            for (int i = 0; i < set.types.size(); i++) {
                LuaType type = set.types.get(i);
                type.serialize(stubOutputStream);
            }
        }
    }

    @Nullable
    public static LuaTypeSet deserialize(@NotNull StubInputStream stubInputStream) throws IOException {
        boolean notNull = stubInputStream.readBoolean();
        if (notNull) {
            LuaTypeSet set = LuaTypeSet.create();
            int num = stubInputStream.readInt();
            for (int i = 0; i < num; i++) {
                LuaType type = new LuaType();
                type.deserialize(stubInputStream);
                set.addType(type);
            }
            return set;
        }
        return null;
    }

    @Override
    public String toString() {
        List<String> list = new SmartList<>();
        for (LuaType type : types) {
            if (!type.isAnonymous()) {
                list.add(type.getClassName());
            }
        }

        return String.join("|", list);
    }

    public String createReturnString() {
        String str = toString();

        return str.isEmpty() ? "void" : str;
    }

    public String createTypeString() {
        String str = toString();

        return str.isEmpty() ? "any" : str;
    }
}
