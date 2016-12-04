package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 *
 * Created by tangzx on 2016/12/3.
 */
public class LuaPsiTreeUtil {

    public interface ElementProcessor<T extends PsiElement> {
        boolean accept(T t);
    }

    /**
     * 向上寻找 local function 定义
     * @param current 当前搜导起点
     * @param processor 处理器
     */
    public static void walkUpLocalFuncDef(PsiElement current, ElementProcessor<LuaNameDef> processor) {
        if (current == null || processor == null)
            return;
        boolean continueSearch = true;
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
                continueSearch = processor.accept(funcName);
            }
        } while (continueSearch && !(curr instanceof PsiFile));
    }

    /**
     * 向上寻找 local 定义
     * @param current 当前搜导起点
     * @param processor 处理器
     */
    public static void walkUpLocalNameDef(PsiElement current, ElementProcessor<LuaNameDef> processor) {
        if (current == null || processor == null)
            return;
        boolean continueSearch = true;
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
                continueSearch = resolveInNameList(nameList, processor);
            }
            else if (curr instanceof LuaLocalFuncDef) {
                LuaLocalFuncDef localFuncDef = (LuaLocalFuncDef) curr;
                //LuaNameDef funcName = localFuncDef.getNameDef();
                //名字部分

                //参数部分
                if (searchParList) continueSearch = resolveInFuncBody(localFuncDef.getFuncBody(), processor);
            }
            else if (curr instanceof LuaGlobalFuncDef) {
                //参数部分
                LuaGlobalFuncDef globalFuncDef = (LuaGlobalFuncDef) curr;
                if (searchParList) continueSearch = resolveInFuncBody(globalFuncDef.getFuncBody(), processor);
            }
            // for name = x, y do end
            else if (curr instanceof LuaForAStat) {
                LuaForAStat forAStat = (LuaForAStat) curr;
                if (searchParList) continueSearch = processor.accept(forAStat.getNameDef());
            }
            // for name in xxx do end
            else if (curr instanceof LuaForBStat) {
                LuaForBStat forBStat = (LuaForBStat) curr;
                if (searchParList) continueSearch = resolveInNameList(forBStat.getNameList(), processor);
            }
        } while (continueSearch && !(curr instanceof PsiFile));
    }

    private static boolean resolveInFuncBody(LuaFuncBody funcBody, ElementProcessor<LuaNameDef> processor) {
        if (funcBody != null) {
            for (LuaParDef parDef : funcBody.getParDefList()) {
                if (!processor.accept(parDef)) return false;
            }
        }
        return true;
    }

    private static boolean resolveInNameList(LuaNameList nameList, ElementProcessor<LuaNameDef> processor) {
        if (nameList != null) {
            for (LuaNameDef nameDef : nameList.getNameDefList()) {
                if (!processor.accept(nameDef)) return false;
            }
        }
        return true;
    }
}
