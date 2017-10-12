/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;
import com.tang.intellij.lua.ty.Ty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 * Created by tangzx on 2016/12/3.
 */
public class LuaPsiTreeUtil {

    public interface ElementProcessor<T extends PsiElement> {
        boolean accept(T t);
    }

    public static void walkUpLabel(PsiElement current, ElementProcessor<LuaLabelStat> processor) {
        PsiElement prev = current.getPrevSibling();
        while (true) {
            if (prev == null)
                prev = current.getParent();
            if (prev == null || prev instanceof PsiFile)
                break;
            if (prev instanceof LuaLabelStat && !processor.accept((LuaLabelStat) prev))
                break;
            current = prev;
            prev = prev.getPrevSibling();
        }
    }

    public static <T extends PsiElement> void walkTopLevelInFile(PsiElement element, Class<T> cls, ElementProcessor<T> processor) {
        if (element == null || processor == null)
            return;
        PsiElement parent = element;
        while (!(parent.getParent() instanceof PsiFile))
            parent = parent.getParent();

        for(PsiElement child = parent; child != null; child = child.getPrevSibling()) {
            if (cls.isInstance(child)) {
                if (!processor.accept(cls.cast(child))) {
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
                LuaLocalFuncDef localFuncDef = (LuaLocalFuncDef) curr;
                continueSearch = processor.accept(localFuncDef);
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
     * 向上寻找 local 定义
     * @param element 当前搜索起点
     * @param processor 处理器
     */
    public static void walkUpLocalNameDef(PsiElement element, ElementProcessor<LuaNameDef> processor) {
        if (element == null || processor == null)
            return;
        boolean continueSearch = true;

        PsiElement curr = element;
        do {
            PsiElement next = curr.getPrevSibling();
            boolean isParent = false;
            if (next == null) {
                next = curr.getParent();
                isParent = true;
            }
            curr = next;

            if (curr instanceof LuaLocalDef) {
                LuaLocalDef localDef = (LuaLocalDef) curr;
                // 跳过类似
                // local name = name //skip
                if (!localDef.getNode().getTextRange().contains(element.getNode().getTextRange())) {
                    LuaNameList nameList = localDef.getNameList();
                    continueSearch = resolveInNameList(nameList, processor);
                }
            } else if (isParent) {
                if (curr instanceof LuaFuncBody) {
                    continueSearch = resolveInFuncBody((LuaFuncBody) curr, processor);
                }
                // for name = x, y do end
                else if (curr instanceof LuaForAStat) {
                    LuaForAStat forAStat = (LuaForAStat) curr;
                    continueSearch = processor.accept(forAStat.getParamNameDef());
                }
                // for name in xxx do end
                else if (curr instanceof LuaForBStat) {
                    LuaForBStat forBStat = (LuaForBStat) curr;
                    continueSearch = resolveInNameList(forBStat.getParamNameDefList(), processor);
                }
            }
        } while (continueSearch && !(curr instanceof PsiFile));
    }

    private static boolean resolveInFuncBody(LuaFuncBody funcBody, ElementProcessor<LuaNameDef> processor) {
        if (funcBody != null) {
            for (LuaParamNameDef parDef : funcBody.getParamNameDefList()) {
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

    private static boolean resolveInNameList(List<LuaParamNameDef> nameList, ElementProcessor<LuaNameDef> processor) {
        if (nameList != null) {
            for (LuaNameDef nameDef : nameList) {
                if (!processor.accept(nameDef)) return false;
            }
        }
        return true;
    }

    @Nullable
    public static <T extends PsiElement> T findElementOfClassAtOffset(@NotNull PsiFile file, int offset, @NotNull Class<T> clazz, boolean strictStart) {
        T t = PsiTreeUtil.findElementOfClassAtOffset(file, offset, clazz, strictStart);
        if (t == null)
            t = PsiTreeUtil.findElementOfClassAtOffset(file, offset - 1, clazz, strictStart);
        return t;
    }

    public static <T extends PsiElement> T getParentOfType(@Nullable PsiElement element, @NotNull Class<T> aClass, @NotNull Class... skips) {
        if (element == null) {
            return null;
        } else {
            element = element.getParent();

            while(element != null && !aClass.isInstance(element) && PsiTreeUtil.instanceOf(element, skips)) {
                if (element instanceof PsiFile) {
                    return null;
                }

                element = element.getParent();
            }

            @SuppressWarnings("unchecked")
            T e = (T) element;
            return e;
        }
    }


    private static final Class[] WS = {PsiWhiteSpace.class};
    private static final Class[] WS_COMMENTS = {PsiWhiteSpace.class, PsiComment.class};

    public static PsiElement skipWhitespacesBackward(@Nullable PsiElement element) {
        return PsiTreeUtil.skipSiblingsBackward(element, WS);
    }

    public static PsiElement skipWhitespacesAndCommentsBackward(@Nullable PsiElement element) {
        return PsiTreeUtil.skipSiblingsBackward(element, WS_COMMENTS);
    }

    public static PsiElement skipWhitespacesForward(@Nullable PsiElement element) {
        return PsiTreeUtil.skipSiblingsForward(element, WS);
    }

    public static PsiElement skipWhitespacesAndCommentsForward(@Nullable PsiElement element) {
        return PsiTreeUtil.skipSiblingsForward(element, WS_COMMENTS);
    }

    @NotNull
    public static ITy findContextClass(PsiElement current) {
        //todo module ty
        while (true) {
            if (current instanceof PsiFile)
                break;
            if (current instanceof LuaClassMethod) {
                LuaClassMethod method = (LuaClassMethod) current;
                return method.guessParentType(new SearchContext(current.getProject()));
            }
            current = current.getParent();
        }
        return Ty.Companion.getUNKNOWN();
    }
}
