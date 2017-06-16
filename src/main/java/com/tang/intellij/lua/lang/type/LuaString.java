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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by tangzx on 2016/12/25.
 */
public class LuaString {
    public int start;
    public int end;
    public String value;

    /**
     * 获取 lua 字符串的内容，
     * "value"
     * 'value'
     * [[value]]
     * @param text string element
     * @return value of String
     */
    public static LuaString getContent(String text) {
        LuaString content = new LuaString();
        if (text.startsWith("[")) {
            Pattern pattern = Pattern.compile("\\[(=*)\\[(.*)]\\1]");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String contentString = matcher.group(2);
                content.start = matcher.start(2);
                content.end = matcher.end(2);
                content.value = contentString;
            }
        } else {
            content.start = 1;
            content.end = text.length() - 1;
            content.value = text.substring(1, text.length() - 1);
        }
        return content;
    }
}
