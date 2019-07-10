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

package com.tang.intellij.lua.unity.ty

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.impl.ProjectLifecycleListener

class UnityShortNamesManager : UnityShortNamesManagerBase(), ProjectLifecycleListener {

    init {
        val application = ApplicationManager.getApplication()
        val connect = application.messageBus.connect()
        connect.subscribe(ProjectLifecycleListener.TOPIC, this)
        application.executeOnPooledThread {
            createSocket()
        }
    }

    override val project: Project
        get() = ProjectManager.getInstance().openProjects.first()

    override fun afterProjectClosed(project: Project) {
        close()
    }

    override fun onParseLibrary() {
        super.onParseLibrary()
        for (project in ProjectManager.getInstance().openProjects) {
            DaemonCodeAnalyzer.getInstance(project).restart()
        }
    }
}