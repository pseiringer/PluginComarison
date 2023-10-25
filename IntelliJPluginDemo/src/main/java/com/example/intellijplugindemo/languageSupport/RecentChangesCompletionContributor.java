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
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        var currentElem = parameters.getOriginalFile().findElementAt(parameters.getOffset());
                        if (currentElem == null)
                            return;

                        var elemBefore = currentElem.getPrevSibling();
                        if (elemBefore == null)
                            return;

                        var textBefore = elemBefore.getText();
                        var possibleDiff = RecentChangesService.getInstance()
                                .getDiff(diff -> diff.getReplacementText().contains(textBefore));
                        if (possibleDiff == null)
                            return;

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
