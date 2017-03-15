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

package com.tang.intellij.lua.psi;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

import java.io.IOException;

/**
 * 方法的参数信息
 * param info
 * Created by tangzx on 2017/2/4.
 */
public class LuaParamInfo {

    private boolean isOptional;
    private String name;
    private String[] types = new String[0];

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public static LuaParamInfo deserialize(StubInputStream stubInputStream) throws IOException {
        LuaParamInfo paramInfo = new LuaParamInfo();
        paramInfo.setName(StringRef.toString(stubInputStream.readName()));
        paramInfo.setOptional(stubInputStream.readBoolean());
        int len = stubInputStream.readByte();
        String[] types = new String[len];
        for (int i = 0; i < len; i++) {
            types[i] = StringRef.toString(stubInputStream.readName());
        }
        paramInfo.setTypes(types);
        return paramInfo;
    }

    public static void serialize(LuaParamInfo param, StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeName(param.getName());
        stubOutputStream.writeBoolean(param.isOptional());
        stubOutputStream.writeByte(param.types.length);
        for (int i = 0; i < param.types.length; i++) {
            stubOutputStream.writeName(param.types[i]);
        }
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }
}
