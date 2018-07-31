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

package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.stubs.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * LuaLookupElement
 * Created by TangZX on 2016/12/16.
 */
public class LuaLookupElement extends LookupElement implements Comparable<LookupElement> {
    private boolean itemTextUnderlined;

    public static void fillTypes(Project project, Collection<LookupElement> results) {
        LuaClassIndex.Companion.processKeys(project, key -> {
            results.add(LookupElementBuilder.create(key).withIcon(LuaIcons.CLASS));
            return true;
        });
    }

    protected String myItemString;
    private String myLookupString;
    private String myTypeText;
    private boolean isBold;
    private Icon myIcon;
    private Icon myTypeIcon;
    private String myTailText;
    private InsertHandler<LookupElement> myHandler;

    private LuaLookupElement(@NotNull final String lookupString,
                             @Nullable final String tailText,
                             @Nullable final String typeText, final boolean bold,
                             @Nullable final Icon icon,
                             @Nullable final Icon typeIcon,
                             @NotNull final InsertHandler<LookupElement> handler) {
        myLookupString = lookupString;
        myTailText = tailText;
        myTypeText = typeText;
        isBold = bold;
        myIcon = icon;
        myTypeIcon = typeIcon;
        myHandler = handler;
    }

    public LuaLookupElement(
            @NotNull final String lookupString,
            final boolean bold,
            @Nullable final Icon icon) {
        this(lookupString, null, null, bold, icon, null, new BasicInsertHandler<>());
    }

    @NotNull
    public String getLookupString() {
        return myLookupString;
    }

    public void setLookupString(String value) {
        myLookupString = value;
    }

    @NotNull
    public String getItemText() {
        if (myItemString != null)
            return myItemString;
        return myLookupString;
    }

    public void setItemText(String myItemString) {
        this.myItemString = myItemString;
    }

    public void setItemTextUnderlined(boolean itemTextUnderlined) {
        this.itemTextUnderlined = itemTextUnderlined;
    }

    @Nullable
    private String getTailText() {
        return !StringUtil.isEmpty(myTailText) ? myTailText : null;
    }

    public void setTailText(String text) {
        myTailText = text;
    }

    @Nullable
    protected String getTypeText() {
        return !StringUtil.isEmpty(myTypeText) ? myTypeText : null;
    }

    protected void setTypeText(String value) {
        myTypeText = value;
    }

    public void setIcon(Icon icon) {
        myIcon = icon;
    }

    public Icon getIcon() {
        return myIcon;
    }

    private Icon getTypeIcon() {
        return myTypeIcon;
    }

    @Override
    public void handleInsert(InsertionContext context) {
        myHandler.handleInsert(context, this);
    }

    public void setHandler(InsertHandler<LookupElement> handler) {
        myHandler = handler;
    }

    public InsertHandler<LookupElement> getHandler() {
        return myHandler;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setItemText(getItemText());
        presentation.setItemTextUnderlined(itemTextUnderlined);
        presentation.setItemTextBold(isBold);
        presentation.setTailText(getTailText(), true);
        presentation.setTypeText(getTypeText(), getTypeIcon());
        presentation.setIcon(getIcon());
    }

    public int compareTo(@NotNull final LookupElement o) {
        return getLookupString().compareTo(o.getLookupString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LuaLookupElement) {
            LuaLookupElement element = (LuaLookupElement) obj;
            return element.hashCode() == hashCode();
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getItemText().hashCode();
    }
}
