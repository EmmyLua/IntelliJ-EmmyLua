package com.tang.intellij.lua.lang.type;

import com.tang.intellij.lua.doc.psi.LuaDocClassDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 类型集合
 * Created by TangZX on 2016/12/4.
 */
public class LuaTypeSet {

    public static LuaTypeSet create(LuaDocClassDef ... classDefs) {
        LuaTypeSet set = new LuaTypeSet();
        for (LuaDocClassDef def : classDefs) {
            set.types.add(LuaType.create(def));
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

    public LuaType getType(int index) {
        return types.get(index);
    }

    public boolean isEmpty() {
        return types.isEmpty();
    }
}
