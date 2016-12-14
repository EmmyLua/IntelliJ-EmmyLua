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
import com.tang.intellij.lua.editor.structure.LuaClassFieldTreeElement;
import com.tang.intellij.lua.editor.structure.LuaClassMethodTreeElement;
import com.tang.intellij.lua.editor.structure.LuaFileTreeElement;
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

        public LuaStructureViewModel(@NotNull PsiFile psiFile) {
            super(psiFile, new LuaFileTreeElement((LuaFile) psiFile));
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
                if (o1 instanceof LuaClassFieldTreeElement && o2 instanceof LuaClassMethodTreeElement)
                    return -1;
                else if (o1 instanceof LuaClassMethodTreeElement && o2 instanceof LuaClassFieldTreeElement)
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
