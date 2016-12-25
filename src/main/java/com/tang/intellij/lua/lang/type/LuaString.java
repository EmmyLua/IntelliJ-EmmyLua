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
     * [[value]] todo
     * @param text string element
     * @return value of String
     */
    public static LuaString getContent(String text) {
        LuaString content = new LuaString();
        if (text.startsWith("[")) {
            Pattern pattern = Pattern.compile("\\[\\[(.+?)\\]\\]");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String contentString = matcher.group(1);
                content.start = 2;
                content.end = text.length() - 2;
                content.value = contentString;
            }
        } else {
            content.start = 1;
            content.value = text.substring(1, text.length() - 1);
        }
        return content;
    }
}
