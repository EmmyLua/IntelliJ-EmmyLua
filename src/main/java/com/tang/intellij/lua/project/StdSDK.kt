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

package com.tang.intellij.lua.project

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl

/**
 *
 * Created by tangzx on 2017/2/6.
 */
class StdSDK : ApplicationComponent {

    override fun initComponent() {
        val pjt = ProjectJdkTable.getInstance()
        //清除旧的std sdk，不用了，用predefined代替
        val mySdk = pjt.findJdk(StdSDK.NAME)
        if (mySdk == null) {
            val sdk = ProjectJdkImpl(StdSDK.NAME, LuaSdkType.instance)
            ApplicationManager.getApplication().runWriteAction { pjt.addJdk(sdk) }
        }
    }

    override fun disposeComponent() {

    }

    override fun getComponentName() = "StdSDK"

    companion object {
        private const val NAME = "Lua"
    }
}
