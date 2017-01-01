package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.tang.intellij.lua.lang.LuaIcons;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/1/1.
 */
public class LuaNamedValue extends XNamedValue {
    public LuaNamedValue(@NotNull String name) {
        super(name);
    }

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        xValueNode.setPresentation(LuaIcons.LOCAL_VAR, "v1", "v2", false);
    }
}
