package com.example.intellijplugindemo.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class EditorContextDialogTest extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Get all the required data from data keys
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Document document = editor.getDocument();

        // Work off of the primary caret to get the selection info
        CaretModel caretModel = editor.getCaretModel();
        VisualPosition primaryPos = caretModel.getPrimaryCaret().getVisualPosition();

        caretModel.addCaret(new VisualPosition(primaryPos.line + 1, primaryPos.column));
    }
}
