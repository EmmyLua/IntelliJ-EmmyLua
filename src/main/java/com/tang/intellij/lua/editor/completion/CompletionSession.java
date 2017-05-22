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

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

/**
 *
 * Created by TangZX on 2017/5/22.
 */
public class CompletionSession {

    private CompletionParameters parameters;
    private CompletionResultSet resultSet;

    public static Key<CompletionSession> KEY = Key.create("lua.CompletionSession");

    public CompletionSession(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {
        parameters = completionParameters;
        resultSet = completionResultSet;
    }

    private HashSet<String> words = new HashSet<>();

    public boolean addWord(@NotNull String word) {
        return words.add(word);
    }

    public boolean containsWord(@NotNull String word) {
        return words.contains(word);
    }

    public CompletionParameters getParameters() {
        return parameters;
    }

    public CompletionResultSet getResultSet() {
        return resultSet;
    }
}
