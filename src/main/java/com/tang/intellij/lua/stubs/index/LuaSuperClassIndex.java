/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tang.intellij.lua.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2017/3/29.
 */
public class LuaSuperClassIndex extends StringStubIndexExtension<LuaDocClassDef> {
    public static final StubIndexKey<String, LuaDocClassDef> KEY = StubIndexKey.createIndexKey("lua.index.super_class");

    private static final LuaSuperClassIndex INSTANCE = new LuaSuperClassIndex();

    @NotNull
    @Override
    public StubIndexKey<String, LuaDocClassDef> getKey() {
        return KEY;
    }

    public static LuaSuperClassIndex getInstance() {
        return INSTANCE;
    }
}
