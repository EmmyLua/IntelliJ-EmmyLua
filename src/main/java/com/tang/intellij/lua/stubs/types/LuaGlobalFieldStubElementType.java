package com.tang.intellij.lua.stubs.types;

import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocGlobalDef;
import com.tang.intellij.lua.comment.psi.impl.LuaDocGlobalDefImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.stubs.index.LuaGlobalFieldIndex;
import com.tang.intellij.lua.stubs.LuaGlobalFieldStub;
import com.tang.intellij.lua.stubs.impl.LuaGlobalFieldStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by tangzx on 2016/12/9.
 */
public class LuaGlobalFieldStubElementType extends IStubElementType<LuaGlobalFieldStub, LuaDocGlobalDef> {

    public LuaGlobalFieldStubElementType() {
        super("Global Field", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaDocGlobalDef createPsi(@NotNull LuaGlobalFieldStub luaGlobalFieldStub) {
        return new LuaDocGlobalDefImpl(luaGlobalFieldStub, LuaElementType.GLOBAL_FIELD_DEF);
    }

    @NotNull
    @Override
    public LuaGlobalFieldStub createStub(@NotNull LuaDocGlobalDef luaDocGlobalDef, StubElement stubElement) {
        List<String> names = new ArrayList<>();

        LuaCommentOwner owner = LuaCommentUtil.findOwner(luaDocGlobalDef);
        if (owner instanceof LuaAssignStat) {
            LuaAssignStat stat = (LuaAssignStat) owner;
            LuaVarList list = stat.getVarList();
            for (LuaVar var : list.getVarList()) {
                LuaNameRef ref = var.getNameRef();
                if (ref != null) {
                    names.add(ref.getText());
                }
            }
        }

        return new LuaGlobalFieldStubImpl(stubElement, names.toArray(new String[names.size()]));
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.global.field";
    }

    @Override
    public void serialize(@NotNull LuaGlobalFieldStub luaGlobalFieldStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        String[] names = luaGlobalFieldStub.getNames();
        stubOutputStream.writeInt(names.length);
        for (String name : names)
            stubOutputStream.writeUTFFast(name);
    }

    @NotNull
    @Override
    public LuaGlobalFieldStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        int length = stubInputStream.readInt();
        String[] names = new String[length];
        for (int i = 0; i < length; i++) {
            names[i] = stubInputStream.readUTFFast();
        }
        return new LuaGlobalFieldStubImpl(stubElement, names);
    }

    @Override
    public void indexStub(@NotNull LuaGlobalFieldStub luaGlobalFieldStub, @NotNull IndexSink indexSink) {
        String[] names = luaGlobalFieldStub.getNames();
        for (String name : names) {
            indexSink.occurrence(LuaGlobalFieldIndex.KEY, name);
        }
    }
}
