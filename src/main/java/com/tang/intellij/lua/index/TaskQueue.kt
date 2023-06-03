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

package com.tang.intellij.lua.index

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.progress.impl.ProgressManagerImpl
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.QueueProcessor
import java.util.function.BiConsumer

interface ContinuableRunnable {
    fun run(continuation: Runnable)
}

class QueueConsumer : BiConsumer<ContinuableRunnable, Runnable> {
    override fun accept(t: ContinuableRunnable, u: Runnable) = t.run(u)
}

class TaskQueue(val project: Project) {
    private var isDisposed = false
    private val processor = QueueProcessor(
        QueueConsumer(),
        true,
        QueueProcessor.ThreadToUse.AWT
    ) { isDisposed }

    fun runReadAction(title: String, action: (indicator: ProgressIndicator) -> Unit) {
        val task = object: Task.Backgroundable(project, title, false) {
            override fun run(indicator: ProgressIndicator) {
                DumbService.getInstance(project).runReadActionInSmartMode {
                    action(indicator)
                }
            }
        }
        run(task)
    }

    fun run(task: Task.Backgroundable) {
        processor.add(object : ContinuableRunnable {
            override fun run(continuation: Runnable) {
                val indicator = BackgroundableProcessIndicator(task)
                val pm = ProgressManager.getInstance() as ProgressManagerImpl
                pm.runProcessWithProgressAsynchronously(
                    task,
                    indicator,
                    {
                        continuation.run()
                    },
                    ModalityState.NON_MODAL)
            }
        })
    }
}