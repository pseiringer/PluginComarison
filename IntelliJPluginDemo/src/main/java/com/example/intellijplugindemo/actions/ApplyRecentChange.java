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

import java.util.ArrayList;

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

        // calculate the position that needs to be replaced
        var idxInPsiElement = found.idxInElement;
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

        //find the closest occurrence of matching diff if it exists multiple times
        // (e.g. inside a string literal which counts as one single token)
        var indices = new ArrayList<Integer>();
        int currIdx = 0;
        while (currIdx >= 0){
            currIdx++;
            currIdx = selectedText.indexOf(matchingDiff.getRemovedText(), currIdx);
            if (currIdx >= 0) indices.add(currIdx);
        }
        var caretIdx = caret.getOffset() - currentElement.getTextOffset();
        int shortestDistance = Math.abs(indices.get(0) - caretIdx);
        int closestIdx = indices.get(0);
        for (int i = 1; i < indices.size(); i++) {
            int currentDistance = Math.abs(indices.get(i) - caretIdx);
            if(currentDistance < shortestDistance){
                closestIdx = indices.get(i);
                shortestDistance = currentDistance;
            }
        }

        return new FindDiffResult(currentElement, matchingDiff, closestIdx);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private class FindDiffResult {
        public final PsiElement currentElement;
        public final RecentChangesService.SimpleDiff matchingDiff;
        public final int idxInElement;
        public FindDiffResult(PsiElement currentElement, RecentChangesService.SimpleDiff matchingDiff, int idxInElement){
            this.currentElement = currentElement;
            this.matchingDiff = matchingDiff;
            this.idxInElement = idxInElement;
        }
    }
}
