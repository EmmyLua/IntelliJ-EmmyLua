package com.tang.intellij.lua.lang;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;

import javax.swing.*;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaIcons {
    public static final Icon FILE = IconLoader.getIcon("/com/tang/intellij/lua/lang/lua-file.png");
    public static final Icon CLASS = AllIcons.Nodes.Class;
    public static final Icon CLASS_FIELD = AllIcons.Nodes.Field;
    public static final Icon CLASS_METHOD = AllIcons.Nodes.Method;
    public static final Icon GLOBAL_FUNCTION = new LayeredIcon(AllIcons.Nodes.Method, AllIcons.Nodes.StaticMark);
    public static final Icon GLOBAL_FIELD = new LayeredIcon(AllIcons.Nodes.Field, AllIcons.Nodes.StaticMark);
    public static final Icon LOCAL_VAR = new LayeredIcon(AllIcons.Nodes.Variable);
    public static final Icon LOCAL_FUNCTION = new LayeredIcon(AllIcons.Nodes.Function, AllIcons.Nodes.C_private);
    public static final Icon WORD = AllIcons.Actions.Edit;
}
