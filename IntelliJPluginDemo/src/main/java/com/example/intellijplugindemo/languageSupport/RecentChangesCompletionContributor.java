package com.example.intellijplugindemo.languageSupport;

import com.example.intellijplugindemo.services.RecentChangesService;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class RecentChangesCompletionContributor extends CompletionContributor {
    public RecentChangesCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(),
                new CompletionProvider<>() {
                    /**
                     * Checks if the replacement text of a change can be applied at this time.
                     * If a fitting change is found it is added to the completion result set.
                     */
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        // get the element at the current offset
                        var currentElem = parameters.getOriginalFile().findElementAt(parameters.getOffset());
                        if (currentElem == null)
                            return;

                        // get the element before it
                        // (since the offset points to the space after the current word)
                        var elemBefore = currentElem.getPrevSibling();
                        if (elemBefore == null)
                            return;

                        // get the relevant text
                        var textBefore = elemBefore.getText();
                        // try to find a valid diff
                        var possibleDiff = RecentChangesService.getInstance()
                                .getDiff(diff -> diff.getReplacementText().contains(textBefore));
                        if (possibleDiff == null)
                            return;

                        // a diff has been found, create a lookup element
                        resultSet.addElement(
                                LookupElementBuilder.create(
                                        possibleDiff.getReplacementText()
                                )
                        );
                    }
                }
        );
    }
}
