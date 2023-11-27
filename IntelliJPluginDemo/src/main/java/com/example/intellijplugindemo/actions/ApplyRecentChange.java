package com.example.intellijplugindemo.actions;

import com.example.intellijplugindemo.services.RecentChangesService;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class ApplyRecentChange extends AnAction {

    /**
     * Checks if a change can be applied at this time
     * applies the change accordingly.
     * @inheritDoc
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
        var editor = event.getRequiredData(CommonDataKeys.EDITOR);

        // try to find a diff
        var found = findDiff(
                editor,
                event.getData(CommonDataKeys.PSI_FILE)
        );

        if (found == null){
            // no diff found
            HintManager.getInstance().showErrorHint(editor, "No matching change detected");
            return;
        }

        //a change has been found
        System.out.println("matching diff found");
        System.out.println("replacing '" + found.matchingDiff.getRemovedText() + "' with '" + found.matchingDiff.getReplacementText() + "'");

        // get text and position of the element
        var selectedText = found.currentElement.getText();
        var psiElemStart = found.currentElement.getTextOffset();

        //replace the text in the document
        var idxInPsiElement = selectedText.indexOf(found.matchingDiff.getRemovedText());
        var replacementStart = psiElemStart + idxInPsiElement;
        var replacementEnd = replacementStart + found.matchingDiff.getRemovedText().length();

        // Replace the selection with a fixed string.
        // Must do this document change in a write action context.
        var project = event.getRequiredData(CommonDataKeys.PROJECT);
        var document = editor.getDocument();
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.replaceString(replacementStart, replacementEnd, found.matchingDiff.getReplacementText())
        );
    }

    /**
     * Checks if a change can be applied at this time and updates
     * the visibility of the action accordingly.
     * @inheritDoc
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        // try to find a diff
        var found = findDiff(
                event.getRequiredData(CommonDataKeys.EDITOR),
                event.getData(CommonDataKeys.PSI_FILE)
        );

        // check if a diff has been found
        if (found == null){
            event.getPresentation().setVisible(false);
        }
        else {
            event.getPresentation().setEnabledAndVisible(true);
        }
    }

    /**
     * Gets the current text (at the primary caret) and tries
     * to find a fitting change.
     * @param editor The active Editor.
     * @param psiFile The active PsiFile.
     * @return A FindDiffResult containing the selected PsiElement and found SimpleDiff
     */
    public FindDiffResult findDiff(Editor editor, PsiFile psiFile){
        //get element text where caret is currently located
        var caret = editor.getCaretModel().getPrimaryCaret();
        var currentElement = psiFile.findElementAt(caret.getOffset());
        if (currentElement == null){
            System.out.println("no replaceable element found");
            return null;
        }

        // get the text of the current element
        var selectedText = currentElement.getText();

        //check if text matches a recent change
        var matchingDiff = RecentChangesService.getInstance().getDiffMatchingRemovedText(selectedText);
        if (matchingDiff == null){
            System.out.println("no matching change detected");
            return null;
        }

        return new FindDiffResult(currentElement, matchingDiff);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private class FindDiffResult {
        public final PsiElement currentElement;
        public final RecentChangesService.SimpleDiff matchingDiff;
        public FindDiffResult(PsiElement currentElement, RecentChangesService.SimpleDiff matchingDiff){
            this.currentElement = currentElement;
            this.matchingDiff = matchingDiff;
        }
    }
}
