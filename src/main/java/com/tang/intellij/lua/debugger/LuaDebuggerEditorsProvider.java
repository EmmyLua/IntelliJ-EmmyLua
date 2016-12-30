package com.tang.intellij.lua.debugger;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.tang.intellij.lua.lang.LuaFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaDebuggerEditorsProvider extends XDebuggerEditorsProvider {
    @NotNull
    @Override
    public FileType getFileType() {
        return LuaFileType.INSTANCE;
    }

    @NotNull
    @Override
    public Document createDocument(@NotNull Project project,
                                   @NotNull String s,
                                   @Nullable XSourcePosition xSourcePosition,
                                   @NotNull EvaluationMode evaluationMode) {
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        return null;
    }
}
