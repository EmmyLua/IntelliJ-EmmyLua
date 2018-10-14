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

package com.tang.intellij.lua.codeInsight;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.Query;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by tangzx on 2017/3/30.
 */
public abstract class LuaLineMarkerNavigator<T extends PsiElement, S extends PsiElement> implements GutterIconNavigationHandler<T> {
    @Override
    public void navigate(MouseEvent mouseEvent, T t) {
        final List<NavigatablePsiElement> navElements = new ArrayList<>();
        Query<S> search = search(t);
        if (search != null) {
            search.forEach(t1 -> {
                navElements.add((NavigatablePsiElement) t1);
            });
            PsiElementListNavigator.openTargets(mouseEvent,
                    navElements.toArray(new NavigatablePsiElement[0]),
                    getTitle(t),
                    null,
                    new DefaultPsiElementCellRenderer());
        }
    }

    protected abstract String getTitle(T elt);

    @Nullable
    protected abstract Query<S> search(T elt);
}
