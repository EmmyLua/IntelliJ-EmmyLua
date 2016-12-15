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

    public static <T extends PsiElement> void walkTopLevelInFile(PsiElement element, Class<T> cls, ElementProcessor<T> processor) {
        if (element == null || processor == null)
            return;
        PsiElement parent = element;
        while (!(parent.getParent() instanceof PsiFile))
            parent = parent.getParent();

        for(PsiElement child = parent; child != null; child = child.getPrevSibling()) {
            if (cls.isInstance(child)) {
                if (!processor.accept((T) child)) {
                    break;
                }
            }
        }
    }

    /**
     * 向上寻找 local function 定义
     * @param current 当前搜导起点
     * @param processor 处理器
     */
    public static void walkUpLocalFuncDef(PsiElement current, ElementProcessor<LuaLocalFuncDef> processor) {
        if (current == null || processor == null)
            return;
        boolean continueSearch = true;
        int treeDeep = 0;
        int funcDeep = 0;
        PsiElement curr = current;
        do {
            if (curr instanceof LuaLocalFuncDef) {
                //todo 第一级local function不能使用
                //if (funcDeep > 0 || treeDeep != 1) {
                    LuaLocalFuncDef localFuncDef = (LuaLocalFuncDef) curr;
                    continueSearch = processor.accept(localFuncDef);
                //}
                funcDeep++;
            }

            PsiElement prevSibling = curr.getPrevSibling();
            if (prevSibling == null) {
                treeDeep++;
                prevSibling = curr.getParent();
            }
            curr = prevSibling;
        } while (continueSearch && !(curr instanceof PsiFile));
    }

    /**
     * 向上寻找 local function 定义
     * @param current 当前搜导起点
     * @param processor 处理器
     */
    public static void walkUpLocalFuncNameDef(PsiElement current, ElementProcessor<LuaNameDef> processor) {
        walkUpLocalFuncDef(current, localFuncDef -> {
            LuaNameDef nameDef = localFuncDef.getNameDef();
            return nameDef == null || processor.accept(nameDef);
        });
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
            // 跳过类似
            // local name = name //skip
            boolean searchLocalDef = true;
            // 跳过类似
            // local name
            // function(name) end //skip
            // name = nil
            boolean searchParList = false;
            PsiElement next = curr.getPrevSibling();
            if (next == null) {
                searchLocalDef = false;
                searchParList = true;
                next = curr.getParent();
            }
            curr = next;

            if (curr instanceof LuaLocalDef) {
                if (searchLocalDef) {
                    LuaNameList nameList = ((LuaLocalDef) curr).getNameList();
                    continueSearch = resolveInNameList(nameList, processor);
                }
            }
            else if (curr instanceof LuaFuncBody) {
                //参数部分
                if (searchParList) continueSearch = resolveInFuncBody((LuaFuncBody) curr, processor);
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
