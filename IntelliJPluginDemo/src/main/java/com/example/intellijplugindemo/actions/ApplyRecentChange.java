package com.example.intellijplugindemo.actions;

import com.example.intellijplugindemo.services.RecentChangesService;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import org.jetbrains.annotations.NotNull;

public class ApplyRecentChange extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        var psiFile = event.getData(CommonDataKeys.PSI_FILE);
        var project = event.getRequiredData(CommonDataKeys.PROJECT);
        var editor = event.getRequiredData(CommonDataKeys.EDITOR);
        var document = editor.getDocument();

        //get element text where caret is currently located
        var caret = editor.getCaretModel().getPrimaryCaret();
        var currentElement = psiFile.findElementAt(caret.getOffset());
        if (currentElement == null){
            System.out.println("no replaceable element found");
            return;
        }
        var selectedText = currentElement.getText();
        var psiElemStart = currentElement.getTextOffset();

        //check if text matches a recent change
        var matchingDiff = RecentChangesService.getInstance().getDiffMatchingRemovedText(selectedText);
        if (matchingDiff == null){
            HintManager.getInstance().showErrorHint(editor, "No matching change detected");
            System.out.println("no matching change detected");
            return;
        }

        //a change has been found
        //replace the text in the document
        System.out.println("matching diff found");
        System.out.println("replacing '" + matchingDiff.getRemovedText() + "' with '" + matchingDiff.getReplacementText() + "'");
        var idxInPsiElement = selectedText.indexOf(matchingDiff.getRemovedText());
        var replacementStart = psiElemStart + idxInPsiElement;
        var replacementEnd = replacementStart + matchingDiff.getRemovedText().length();

        // Replace the selection with a fixed string.
        // Must do this document change in a write action context.
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.replaceString(replacementStart, replacementEnd, matchingDiff.getReplacementText())
        );
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        var psiFile = event.getData(CommonDataKeys.PSI_FILE);
        var editor = event.getRequiredData(CommonDataKeys.EDITOR);
        var project = event.getRequiredData(CommonDataKeys.PROJECT);

        //get element text where caret is currently located
        var caret = editor.getCaretModel().getPrimaryCaret();
        var currentElement = psiFile.findElementAt(caret.getOffset());
        if (currentElement == null){
            System.out.println("no replaceable element found");
            event.getPresentation().setVisible(false);
            return;
        }
        var selectedText = currentElement.getText();

        //check if text matches a recent change
        var matchingDiff = RecentChangesService.getInstance().getDiffMatchingRemovedText(selectedText);
        if (matchingDiff == null){
            System.out.println("no matching change detected");
            event.getPresentation().setVisible(false);
            return;
        }

        //found a match
        event.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
