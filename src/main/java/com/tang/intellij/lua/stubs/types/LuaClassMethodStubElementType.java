package com.tang.intellij.lua.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.psi.impl.LuaClassMethodDefImpl;
import com.tang.intellij.lua.stubs.LuaClassMethodStub;
import com.tang.intellij.lua.stubs.impl.LuaClassMethodStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/4.
 */
public class LuaClassMethodStubElementType extends IStubElementType<LuaClassMethodStub, LuaClassMethodDef> {
    public LuaClassMethodStubElementType() {
        super("LuaClassMethodStubElementType", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaClassMethodDef createPsi(@NotNull LuaClassMethodStub luaClassMethodStub) {
        return new LuaClassMethodDefImpl(luaClassMethodStub, LuaElementType.CLASS_METHOD_DEF);
    }

    @NotNull
    @Override
    public LuaClassMethodStub createStub(@NotNull LuaClassMethodDef luaClassMethodFuncDef, StubElement stubElement) {
        LuaClassMethodName methodName = luaClassMethodFuncDef.getClassMethodName();
        PsiElement id = methodName.getNameDef();
        PsiElement prefix = methodName.getNameRef();
        assert prefix != null;
        String clazzName = resolveClassName(prefix.getText(), luaClassMethodFuncDef);

        PsiElement prev = id.getPrevSibling();
        boolean isStatic = prev.getNode().getElementType() == LuaTypes.DOT;

        return new LuaClassMethodStubImpl(clazzName, isStatic, stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class_method";
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        //确定是完整的，并且是 class:method, class.method 形式的， 否则会报错
        LuaClassMethodDef psi = (LuaClassMethodDef) node.getPsi();
        LuaClassMethodName classMethodName = psi.getClassMethodName();
        return classMethodName.getNameRef() != null && psi.getFuncBody() != null;
    }

    @Override
    public void serialize(@NotNull LuaClassMethodStub luaClassMethodStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        String name = luaClassMethodStub.getClassName();
        stubOutputStream.writeBoolean(name != null);
        if (name != null) {
            stubOutputStream.writeUTFFast(luaClassMethodStub.getClassName());
        }
        stubOutputStream.writeBoolean(luaClassMethodStub.isStatic());
    }

    @NotNull
    @Override
    public LuaClassMethodStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        boolean hasName = stubInputStream.readBoolean();
        String name = null;
        if (hasName)
            name = stubInputStream.readUTFFast();
        boolean isStatic = stubInputStream.readBoolean();
        return new LuaClassMethodStubImpl(name, isStatic, stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaClassMethodStub luaClassMethodStub, @NotNull IndexSink indexSink) {
        String className = luaClassMethodStub.getClassName();
        if (className != null) {
            if (luaClassMethodStub.isStatic())
                indexSink.occurrence(LuaClassMethodIndex.KEY, className + ".static");
            else
                indexSink.occurrence(LuaClassMethodIndex.KEY, className);
        }
    }

    private static String clazzNameToSearch;
    private static String resolveClassName(String prefixName, LuaClassMethodDef luaClassMethodFuncDef) {
        clazzNameToSearch = null;

        // 寻找Local定义
        LuaPsiTreeUtil.walkTopLevelInFile(luaClassMethodFuncDef, LuaLocalDef.class, luaLocalDef -> {

            LuaNameList nameList = luaLocalDef.getNameList();
            if (nameList != null) {
                LuaNameDef nameDef = PsiTreeUtil.findChildOfType(nameList, LuaNameDef.class);
                if (nameDef != null && prefixName.equals(nameDef.getName())) {
                    //find LuaDocClassDef in doc comment
                    LuaComment comment = luaLocalDef.getComment();
                    if (comment != null) {
                        LuaDocClassDef classDef = PsiTreeUtil.findChildOfType(comment, LuaDocClassDef.class);
                        if (classDef != null) {
                            clazzNameToSearch = classDef.getClassNameText();
                            return false;
                        }
                    }
                    return false;
                }
            }
            return true;
        });

        //如果全局定义的对象的方法，则优先找本文件内的 assign stat
        if (clazzNameToSearch == null) {
            LuaPsiTreeUtil.walkTopLevelInFile(luaClassMethodFuncDef, LuaAssignStat.class, luaAssignStat -> {
                LuaVarList varList = luaAssignStat.getVarList();
                for (PsiElement child = varList.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child instanceof LuaVar) {
                        LuaVar var = (LuaVar) child;
                        if (var.getNameRef() != null && prefixName.equals(var.getNameRef().getName())) {
                            LuaComment comment = luaAssignStat.getComment();
                            if (comment != null) {
                                LuaDocClassDef classDef = PsiTreeUtil.findChildOfType(comment, LuaDocClassDef.class);
                                if (classDef != null) {
                                    clazzNameToSearch = classDef.getClassNameText();
                                    return false;
                                }
                            }
                        }
                        break;
                    }
                }
                return true;
            });
        }
        //TODO search global index
        return clazzNameToSearch;
    }
}
