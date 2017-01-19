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

package com.tang.intellij.lua.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import org.jetbrains.annotations.Nullable;

/**
 * Documentation support
 * Created by tangzx on 2016/12/10.
 */
public class LuaDocumentationProvider extends AbstractDocumentationProvider implements DocumentationProvider {

    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        //TODO Documentation support
        if (element instanceof LuaCommentOwner) {
            LuaComment comment = LuaCommentUtil.findComment((LuaCommentOwner) element);
            if (comment != null) {
                return comment.getText();
            }
        }
        return super.generateDoc(element, originalElement);
    }
}
