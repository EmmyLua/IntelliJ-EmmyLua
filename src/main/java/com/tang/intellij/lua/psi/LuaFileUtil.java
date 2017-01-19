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

package com.tang.intellij.lua.psi;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

/**
 *
 * Created by tangzx on 2017/1/4.
 */
public class LuaFileUtil {

    public static VirtualFile findFile(Project project, String shortUrl) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            String[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRootUrls();
            for (String sourceRoot : sourceRoots) {
                VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(sourceRoot + "/" + shortUrl);
                if (file != null) {
                    return file;
                }
            }
        }
        return null;
    }

    public static String getShortUrl(Project project, VirtualFile file) {
        String fileFullUrl = file.getUrl();
        String fileShortUrl = null;

        Module[] modules = ModuleManager.getInstance(project).getModules();
        moduleLoop: for (Module module : modules) {
            VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            for (VirtualFile sourceRoot : sourceRoots) {
                String sourceRootUrl = sourceRoot.getUrl();
                if (fileFullUrl.startsWith(sourceRootUrl)) {
                    fileShortUrl = fileFullUrl.substring(sourceRootUrl.length() + 1);
                    break moduleLoop;
                }
            }
        }
        return fileShortUrl;
    }

}
