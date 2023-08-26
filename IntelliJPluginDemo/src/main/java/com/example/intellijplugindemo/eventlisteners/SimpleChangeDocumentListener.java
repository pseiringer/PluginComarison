package com.example.intellijplugindemo.eventlisteners;

import com.example.intellijplugindemo.services.RecentChangesService;
import com.example.intellijplugindemo.util.diff_match_patch;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SimpleChangeDocumentListener implements DocumentListener {


    private long delay = 750L;
    private boolean timerActive = false;
    private TimerTask doneTyping;
    private Timer timer = new Timer("Timer");

    private Project project;
    private RecentChangesService recentChanges;

    private String textBeforeChange;
    private diff_match_patch dmp;

    public SimpleChangeDocumentListener(Project project){
        this.project = project;
        recentChanges = RecentChangesService.getInstance();

        dmp = new diff_match_patch();
        dmp.Diff_Timeout = 3;
//        dmp.Diff_EditCost = 4;
    }

    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        if (!timerActive){
            //a new typing action has started
            System.out.println("started typing");
            textBeforeChange = getOriginalTextFromDocument(event.getDocument());
        }
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        System.out.println("typing...");

        if (timerActive){
            //cancel currently active timer
            doneTyping.cancel();
        }
        timerActive = true;
        doneTyping = getDoneTyping(event);
        timer.schedule(doneTyping, delay);
    }

    private TimerTask getDoneTyping(DocumentEvent event) {
        return new TimerTask() {
            public void run() {
                timerActive = false;
                System.out.println("done typing");

                var diffs = dmp.diff_main(textBeforeChange, getOriginalTextFromDocument(event.getDocument()));

                dmp.diff_cleanupSemantic(diffs);

                var changes = diffs.stream()
                        .filter(x -> x.operation != diff_match_patch.Operation.EQUAL)
                        .collect(Collectors.toList());
                if(changes.size() == 2
                        && changes.stream().anyMatch(x -> x.operation == diff_match_patch.Operation.INSERT)
                        && changes.stream().anyMatch(x -> x.operation == diff_match_patch.Operation.DELETE)){
                    //simple change (insert + delete) detected
                    //insert the simplified diff into recent changes
                    System.out.println("simple change detected");
                    var simpleDiff = new RecentChangesService.SimpleDiff();
                    for (var change : changes) {
                        if(change.operation == diff_match_patch.Operation.INSERT)
                            simpleDiff.setReplacementText(change.text);
                        else
                            simpleDiff.setRemovedText(change.text);
                    }
                    recentChanges.addChange(simpleDiff);
                }

                //recentChanges.printChanges();
            }
        };
    }


    private String getOriginalTextFromDocument(Document document){
        CompletableFuture<String> future = new CompletableFuture();

        ApplicationManager.getApplication().runReadAction(() -> {
            future.complete(PsiDocumentManager
                    .getInstance(project)
                    .getPsiFile(document)
                    .getOriginalFile()
                    .getText());
        });

        return future.join();
    }

}
