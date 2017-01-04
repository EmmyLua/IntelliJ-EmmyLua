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
