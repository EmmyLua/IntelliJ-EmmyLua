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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.SmartList;
import com.tang.intellij.lua.ext.ILuaFileResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * Created by tangzx on 2017/1/4.
 */
public class LuaFileUtil {

    //有些扩展名也许是txt
    private static String[] extensions = new String[] {
            ".lua.txt",
            ".lua",
            ".txt",
            ""
    };

    public static boolean fileEquals(VirtualFile f1, VirtualFile f2) {
        if (f1 == null || f2 == null)
            return false;
        //f1.getName().equals(f2.getName()) &&
        return f1.getPath().equals(f2.getPath());
    }

    public static List<String> getAllAvailablePathsForMob(@Nullable String shortPath, @NotNull VirtualFile file) {
        SmartList<String> list = new SmartList<>();
        String fullPath = file.getCanonicalPath();
        if (fullPath != null) {
            for (String ext : extensions) {
                if (!fullPath.endsWith(ext)) {
                    continue;
                }
                list.add(fullPath.substring(0, fullPath.length() - ext.length()));
            }
        }
        if (shortPath != null) {
            for (String ext : extensions) {
                if (!shortPath.endsWith(ext)) {
                    continue;
                }
                String path = shortPath.substring(0, shortPath.length() - ext.length());
                list.add(path);
                if (path.indexOf('/') != -1)
                    list.add(path.replace('/', '.'));
            }
        }
        return list;
    }

    @Nullable
    public static VirtualFile findFile(@NotNull Project project, String shortUrl) {
        if (shortUrl == null)
            return null;
        return ILuaFileResolver.Companion.findLuaFile(project, shortUrl, extensions);
    }

    public static String getShortPath(Project project, VirtualFile file) {
        return VfsUtil.urlToPath(getShortUrl(project, file));
    }

    private static String getShortUrl(Project project, VirtualFile file) {
        String fileFullUrl = file.getUrl();
        String fileShortUrl = fileFullUrl;

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

    @Nullable
    private static VirtualFile getSourceRoot(Project project, VirtualFile file) {
        String fileFullUrl = file.getUrl();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            for (VirtualFile sourceRoot : sourceRoots) {
                String sourceRootUrl = sourceRoot.getUrl();
                if (fileFullUrl.startsWith(sourceRootUrl)) {
                    return sourceRoot;
                }
            }
        }
        return null;
    }

    @Nullable
    public static VirtualFile getPluginVirtualDirectory() {
        IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("com.tang"));
        if (descriptor != null) {
            File pluginPath = descriptor.getPath();

            String url = VfsUtil.pathToUrl(pluginPath.getAbsolutePath());

            return VirtualFileManager.getInstance().findFileByUrl(url);
        }

        return null;
    }

    @Nullable
    public static String getPluginVirtualFile(String path) {
        VirtualFile directory = getPluginVirtualDirectory();
        if (directory != null) {
            String fullPath = directory.getPath() + "/classes/" + path;
            if (new File(fullPath).exists())
                return fullPath;
            fullPath = directory.getPath() + "/" + path;
            if (new File(fullPath).exists())
                return fullPath;
        }
        return null;
    }

    @Nullable
    public static String asRequirePath(@NotNull Project project, @NotNull VirtualFile file) {
        VirtualFile root = getSourceRoot(project, file);
        if (root == null)
            return null;
        ArrayList<String> list = new ArrayList<>();
        VirtualFile item = file;
        while (!item.equals(root)) {
            if (item.isDirectory())
                list.add(item.getName());
            else
                list.add(FileUtil.getNameWithoutExtension(item.getName()));
            item = item.getParent();
        }
        if (list.isEmpty())
            return null;
        Collections.reverse(list);
        return String.join(".", list);
    }

    public static Key<Boolean> PREDEFINED_KEY = Key.create("lua.lib.predefined");

    public static boolean isStdLibFile(@NotNull VirtualFile file, Project project) {
        return file.getUserData(PREDEFINED_KEY) != null || FileIndexFacade.getInstance(project).isInLibraryClasses(file);
    }

    @Nullable
    public static String getArchExeFile() {
        return getPluginVirtualFile("debugger/windows/x86/emmy.arch.exe");
    }
}
