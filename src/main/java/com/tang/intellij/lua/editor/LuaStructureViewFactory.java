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

package com.tang.intellij.lua.editor;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.structureView.*;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.ide.util.treeView.smartTree.SorterUtil;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.tang.intellij.lua.editor.structure.LuaClassFieldElement;
import com.tang.intellij.lua.editor.structure.LuaFileElement;
import com.tang.intellij.lua.editor.structure.LuaFuncElement;
import com.tang.intellij.lua.psi.LuaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * Structure View
 * Created by TangZX on 2016/12/13.
 */
public class LuaStructureViewFactory implements PsiStructureViewFactory {
    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder(PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder(){

            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new LuaStructureViewModel(psiFile);
            }
        };
    }

    class LuaStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {

        LuaStructureViewModel(@NotNull PsiFile psiFile) {
            super(psiFile, new LuaFileElement((LuaFile) psiFile));
            withSorters(new LuaAlphaSorter());
        }

        @Override
        public boolean isAlwaysShowsPlus(StructureViewTreeElement structureViewTreeElement) {
            return false;
        }

        @Override
        public boolean isAlwaysLeaf(StructureViewTreeElement structureViewTreeElement) {
            return false;
        }
    }

    /**
     * 字母alpha排序，但field排在method前面
     */
    class LuaAlphaSorter implements Sorter {

        @Override
        public Comparator getComparator() {
            return (o1, o2) -> {
                if (o1 instanceof LuaClassFieldElement && o2 instanceof LuaFuncElement)
                    return -1;
                else if (o1 instanceof LuaFuncElement && o2 instanceof LuaClassFieldElement)
                    return 1;

                String s1 = SorterUtil.getStringPresentation(o1);
                String s2 = SorterUtil.getStringPresentation(o2);
                return s1.compareToIgnoreCase(s2);
            };
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @NotNull
        @Override
        public ActionPresentation getPresentation() {
            return new ActionPresentationData(IdeBundle.message("action.sort.alphabetically"), IdeBundle.message("action.sort.alphabetically"), AllIcons.ObjectBrowser.Sorted);
        }

        @NotNull
        @Override
        public String getName() {
            return "Alpha Sorter";
        }
    }
}
