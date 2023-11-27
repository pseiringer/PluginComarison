package com.example.intellijplugindemo.eventlisteners;

import com.example.intellijplugindemo.services.RecentChangesService;
import com.example.intellijplugindemo.services.RecentChangesSettingsService;
import com.example.intellijplugindemo.util.DiffWordModeExtender;
import com.example.intellijplugindemo.util.diff_match_patch;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SimpleChangeDocumentListener implements DocumentListener {

    private boolean timerActive = false;
    private TimerTask doneTyping;
    private Timer timer = new Timer("Timer");


    private String textBeforeChange;
    private diff_match_patch dmp;

    public SimpleChangeDocumentListener(){
        // initialize the diff_match_patch algorithm
        dmp = new diff_match_patch();
        dmp.Diff_Timeout = 3;
//        dmp.Diff_EditCost = 4;
    }

    /**
     * Get the unchanged text before a document is changed for the first time.
     * @inheritDoc
     */
    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        if (!timerActive){
            // a new typing action has started
            System.out.println("started typing");
            // get the current text
            var currentText = getOriginalTextFromDocument(event.getDocument());
            if (currentText != null) {
                textBeforeChange = currentText;
            }
        }
    }

    /**
     * Starts a debounce timer to call doneTyping on document change events.
     * @inheritDoc
     */
    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        System.out.println("typing...");

        // check if a typing action is already in progress
        if (timerActive){
            // cancel currently active timer
            doneTyping.cancel();
        }
        // start a new typing action with its timer
        timerActive = true;
        doneTyping = getDoneTyping(event);
        timer.schedule(doneTyping, RecentChangesSettingsService.getInstance().getDebounceTimeMs());
    }

    /**
     * Creates a new TimerTask that handles when a typing action has finished.
     * @param event The last document changed event in the typing action.
     * @return A TimerTask handling the finished typing action.
     */
    private TimerTask getDoneTyping(DocumentEvent event) {
        return new TimerTask() {
            public void run() {
                // typing action has finished
                timerActive = false;
                System.out.println("done typing");

                // get current text
                var currentText = getOriginalTextFromDocument(event.getDocument());
                if (currentText == null) {
                    return;
                }

//                //  calculate fully accurate diff
//                //  sadly the cleanupSemantic feature does not work properly
//                var diffs = dmp.diff_main(textBeforeChange, getOriginalTextFromDocument(event.getDocument()));
//                dmp.diff_cleanupSemantic(diffs);

                // use custom word mode instead
                var diffs = DiffWordModeExtender.diff_wordMode(dmp, textBeforeChange, currentText);

                // check if the found diffs represent a simple change
                var changes = diffs.stream()
                        .filter(x -> x.operation != diff_match_patch.Operation.EQUAL)
                        .collect(Collectors.toList());
                if(changes.size() == 2
                        && changes.stream().anyMatch(x -> x.operation == diff_match_patch.Operation.INSERT)
                        && changes.stream().anyMatch(x -> x.operation == diff_match_patch.Operation.DELETE)){
                    // simple change (insert + delete) detected
                    System.out.println("simple change detected");
                    // create a SimpleDiff object
                    var simpleDiff = new RecentChangesService.SimpleDiff();
                    for (var change : changes) {
                        if(change.operation == diff_match_patch.Operation.INSERT)
                            simpleDiff.setReplacementText(change.text);
                        else
                            simpleDiff.setRemovedText(change.text);
                    }
                    //insert SimpleDiff into recent changes
                    RecentChangesService.getInstance().addChange(simpleDiff);
                }

                RecentChangesService.getInstance().printChanges();
            }
        };
    }

    /**
     * Get the original text from the given document that does not contain the DUMMY_IDENTIFIER ("IntellijIdeaRulezzz ")
     * @param document The document to get the text from.
     * @return The original text of the document.
     */
    private String getOriginalTextFromDocument(Document document){
        CompletableFuture<String> future = new CompletableFuture();

        DataManager.getInstance()
                .getDataContextFromFocusAsync()
                .onSuccess(dataContext -> {
                    var project = dataContext.getData(CommonDataKeys.PROJECT);
                    ApplicationManager.getApplication().runReadAction(() -> {
                        var psiFile = PsiDocumentManager
                                .getInstance(project)
                                .getPsiFile(document);
                        future.complete(
                                (psiFile == null) ? null : psiFile.getOriginalFile().getText()
                        );
                    });
                })
                .onError(e -> {
                    future.complete(null);
                });

        return future.join();
    }

}
