package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.function.Consumer;

/**
 *
 * Created by tangzx on 2016/12/3.
 */
public class LuaPsiTreeUtil {

    /**
     * 向上寻找 local function 定义
     * @param current 当前搜导起点
     * @param processor 处理器
     */
    public static void walkUpLocalFuncDef(PsiElement current, Consumer<LuaNameDef> processor) {
        PsiElement curr = current;
        do {
            PsiElement next = curr.getPrevSibling();
            if (next == null) {
                next = curr.getParent();
            }
            curr = next;

            if (curr instanceof LuaLocalFuncDef) {
                LuaLocalFuncDef localFuncDef = (LuaLocalFuncDef) curr;
                LuaNameDef funcName = localFuncDef.getNameDef();
                //名字部分
                processor.accept(funcName);
            }
        } while (!(curr instanceof PsiFile));
    }

    /**
     * 向上寻找 local 定义
     * @param current 当前搜导起点
     * @param processor 处理器
     */
    public static void walkUpLocalNameDef(PsiElement current, Consumer<LuaNameDef> processor) {

        PsiElement curr = current;
        do {
            boolean searchParList = false;
            PsiElement next = curr.getPrevSibling();
            if (next == null) {
                searchParList = true;
                next = curr.getParent();
            }
            curr = next;

            if (curr instanceof LuaLocalDef) {
                LuaNameList nameList = ((LuaLocalDef) curr).getNameList();
                resolveInNameList(nameList, processor);
            }
            else if (curr instanceof LuaLocalFuncDef) {
                LuaLocalFuncDef localFuncDef = (LuaLocalFuncDef) curr;
                //LuaNameDef funcName = localFuncDef.getNameDef();
                //名字部分

                //参数部分
                if (searchParList) resolveInFuncBody(localFuncDef.getFuncBody(), processor);
            }
            else if (curr instanceof LuaGlobalFuncDef) {
                //参数部分
                LuaGlobalFuncDef globalFuncDef = (LuaGlobalFuncDef) curr;
                if (searchParList) resolveInFuncBody(globalFuncDef.getFuncBody(), processor);
            }
            // for name = x, y do end
            else if (curr instanceof LuaForAStat) {
                LuaForAStat forAStat = (LuaForAStat) curr;
                processor.accept(forAStat.getNameDef());
            }
            // for name in xxx do end
            else if (curr instanceof LuaForBStat) {
                LuaForBStat forBStat = (LuaForBStat) curr;
                resolveInNameList(forBStat.getNameList(), processor);
            }
        } while (!(curr instanceof PsiFile));
    }

    private static void resolveInFuncBody(LuaFuncBody funcBody, Consumer<LuaNameDef> processor) {
        if (funcBody != null) {
            funcBody.getParDefList().forEach(processor);
        }
    }

    private static void resolveInNameList(LuaNameList nameList, Consumer<LuaNameDef> processor) {
        if (nameList != null) {
            nameList.getNameDefList().forEach(processor);
        }
    }
}
