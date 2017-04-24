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

package completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.Arrays;
import java.util.List;

/**
 *
 * Created by tangzx on 2017/4/23.
 */
public class TestCompletion extends LightCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/main/testData/completion";
    }

    public void testLocalCompletion() {
        myFixture.configureByFiles("testCompletion.lua");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();

        assertNotNull(strings);
        assertTrue(strings.containsAll(Arrays.asList("a", "b")));
    }

    public void testGlobalCompletion() {
        //test 1
        myFixture.configureByFiles("globals.lua");
        myFixture.configureByText("test.lua", "<caret>");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();

        assertNotNull(strings);
        assertTrue(strings.containsAll(Arrays.asList("gVar1", "gVar2")));

        //test 2
        myFixture.configureByFiles("globals.lua");
        myFixture.configureByText("test.lua", "gVar2.<caret>");
        myFixture.complete(CompletionType.BASIC, 1);
        strings = myFixture.getLookupElementStrings();

        assertNotNull(strings);
        assertTrue(strings.containsAll(Arrays.asList("aaa", "bbb", "ccc")));
    }

    public void testSelfCompletion() {
        myFixture.configureByFiles("testSelf.lua");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();

        assertNotNull(strings);
        assertTrue(strings.containsAll(Arrays.asList("self:aaa", "self:abb")));
    }

    public void testParamCompletion() {
        myFixture.configureByFiles("testParam.lua");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();

        assertNotNull(strings);
        assertTrue(strings.containsAll(Arrays.asList("param1", "param2")));
    }
}
