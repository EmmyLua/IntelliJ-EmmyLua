package com.tang.intellij.lua.editor;

import com.intellij.ide.structureView.*;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.tang.intellij.lua.editor.structure.LuaFileTreeElement;
import com.tang.intellij.lua.psi.LuaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                return new SimpleStructureViewModel(psiFile);
            }
        };
    }

    class SimpleStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {

        public SimpleStructureViewModel(@NotNull PsiFile psiFile) {
            super(psiFile, new LuaFileTreeElement((LuaFile) psiFile));
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
}
