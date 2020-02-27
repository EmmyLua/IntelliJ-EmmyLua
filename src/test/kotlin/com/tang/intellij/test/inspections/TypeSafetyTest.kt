/*
 * Copyright (c) 2020
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

package com.tang.intellij.test.inspections

import com.tang.intellij.lua.codeInsight.inspection.AssignTypeInspection
import com.tang.intellij.lua.codeInsight.inspection.MatchFunctionSignatureInspection
import com.tang.intellij.lua.codeInsight.inspection.ReturnTypeInspection
import com.tang.intellij.lua.codeInsight.inspection.doc.GenericParameterShadowed
import com.tang.intellij.lua.lang.LuaLanguageLevel
import com.tang.intellij.lua.project.LuaSettings

class TypeSafetyTest : LuaInspectionsTestBase(AssignTypeInspection(), GenericParameterShadowed(), MatchFunctionSignatureInspection(), ReturnTypeInspection()) {
    fun testBoolean() {
        checkByFile("boolean.lua")
    }

    fun testClass() {
        checkByFile("class.lua")
    }

    fun testFunctionClosures() {
        checkByFile("function_closures.lua")
    }

    fun testFunctionCovariance() {
        checkByFile("function_covariance.lua")
    }

    fun testFunctionGenerics() {
        checkByFile("function_generics.lua")
    }

    fun testFunctionGenericsScope() {
        checkByFile("function_generics_scope.lua")
    }

    fun testFunctionMultipleReturns() {
        checkByFile("function_multiple_returns.lua", true, false, true)
    }

    fun testGenericClassConstraints() {
        checkByFile("generic_class_constraints.lua")
    }

    fun testGenericClassCovariance() {
        checkByFile("generic_class_covariance.lua")
    }

    fun testGenericClassFields() {
        checkByFile("generic_class_fields.lua")
    }

    fun testGenericClassScope() {
        checkByFile("generic_class_scope.lua")
    }

    fun testImplicitTypes() {
        checkByFile("implicit_types.lua")
    }

    fun testLambdaParams() {
        checkByFile("lambda_params.lua")
    }

    fun testLocalDefAssignment() {
        checkByFile("local_def_assignment.lua")
    }

    fun testModules() {
        myFixture.configureByFiles("moduleA.lua", "moduleA_reference.lua")
        LuaSettings.instance.languageLevel = LuaLanguageLevel.LUA51
        enableInspection()
        myFixture.checkHighlighting(true, false, false)
    }
}
