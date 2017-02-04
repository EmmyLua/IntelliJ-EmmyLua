/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tang.intellij.lua.stubs;

import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaFuncBodyOwner;

/**
 * func body owner stub
 * Created by TangZX on 2017/2/4.
 */
public interface LuaFuncBodyOwnerStub<T extends LuaFuncBodyOwner> extends StubElement<T> {
    LuaTypeSet getReturnType();
}
