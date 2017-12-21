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

package com.tang.intellij.lua.editor

import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.ide.util.treeView.smartTree.SorterUtil
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.tang.intellij.lua.editor.structure.LuaClassFieldElement
import com.tang.intellij.lua.editor.structure.LuaFileElement
import com.tang.intellij.lua.editor.structure.LuaFuncElement
import com.tang.intellij.lua.psi.LuaPsiFile

/**
 * Structure View
 * Created by TangZX on 2016/12/13.
 */
class LuaStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        return object : TreeBasedStructureViewBuilder() {

            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                return LuaStructureViewModel(psiFile)
            }
        }
    }

    inner class LuaStructureViewModel(psiFile: PsiFile) : StructureViewModelBase(psiFile, LuaFileElement(psiFile as LuaPsiFile)), StructureViewModel.ElementInfoProvider {

        init {
            withSorters(LuaAlphaSorter())
        }

        override fun isAlwaysShowsPlus(structureViewTreeElement: StructureViewTreeElement): Boolean {
            return false
        }

        override fun isAlwaysLeaf(structureViewTreeElement: StructureViewTreeElement): Boolean {
            return false
        }
    }

    /**
     * 字母alpha排序，但field排在method前面
     */
    inner class LuaAlphaSorter : Sorter {

        override fun getComparator() = kotlin.Comparator<Any> { o1, o2 ->
            if (o1 is LuaClassFieldElement && o2 is LuaFuncElement)
                return@Comparator -1
            else if (o1 is LuaFuncElement && o2 is LuaClassFieldElement)
                return@Comparator 1

            val s1 = SorterUtil.getStringPresentation(o1)
            val s2 = SorterUtil.getStringPresentation(o2)
            s1.compareTo(s2, ignoreCase = true)
        }

        override fun isVisible(): Boolean {
            return true
        }

        override fun getPresentation(): ActionPresentation {
            return ActionPresentationData(IdeBundle.message("action.sort.alphabetically"), IdeBundle.message("action.sort.alphabetically"), AllIcons.ObjectBrowser.Sorted)
        }

        override fun getName(): String {
            return "Alpha Sorter"
        }
    }
}
