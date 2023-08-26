package com.example.intellijplugindemo.services;

import com.google.common.collect.EvictingQueue;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;

import java.util.Objects;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class RecentChangesService implements Disposable {

    // list of recent changes
    private Queue<SimpleDiff> recentChanges = EvictingQueue.create(10);
    public void addChange(SimpleDiff change) {
        recentChanges.add(change);
    }

    public void printChanges(){
        System.out.print("Changes: [");
        for (var change :
                recentChanges) {
            System.out.print("{'" + change.removedText + "' -> '" + change.replacementText + "'} ");
        }
        System.out.println("]");
    }

    public Queue<SimpleDiff> getRecentChanges() {
        return recentChanges;
    }

    public SimpleDiff getMatchingDiff(String text) {
        var reverseIterator = recentChanges.stream()
                .collect(Collectors.toCollection(ArrayDeque::new))
                .descendingIterator();
        while(reverseIterator.hasNext()) {
            var nextDiff = reverseIterator.next();
            if(text.contains(nextDiff.getRemovedText())){
                return nextDiff;
            }
        }
        return null;
    }

    public static RecentChangesService getInstance(){
        return ApplicationManager.getApplication().getService(RecentChangesService.class);
    }

    @Override
    public void dispose() {
        /* nothing to do here */
    }


    public static class SimpleDiff{
        private String removedText;
        private String replacementText;

        public String getRemovedText() {
            return removedText;
        }

        public void setRemovedText(String removedText) {
            this.removedText = removedText;
        }

        public String getReplacementText() {
            return replacementText;
        }

        public void setReplacementText(String replacementText) {
            this.replacementText = replacementText;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleDiff that = (SimpleDiff) o;
            return Objects.equals(removedText, that.removedText) && Objects.equals(replacementText, that.replacementText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(removedText, replacementText);
        }
    }

}
