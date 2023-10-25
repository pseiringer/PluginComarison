package com.example.intellijplugindemo.services;

import com.google.common.collect.EvictingQueue;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service(Service.Level.APP)
public final class RecentChangesService implements Disposable {

    // list of recent changes
    private Queue<SimpleDiff> recentChanges;
    private List<RecentDiffsChangedListener> changeListeners = new ArrayList<>();

    public RecentChangesService(){
        var settings = RecentChangesSettingsService.getInstance();
        recentChanges = EvictingQueue.create(settings.getQueueSize());
        settings.addQueueSizeListener(size -> resizeChangesQueue(size));
    }

    public void addChange(SimpleDiff change) {
        recentChanges.add(change);
        notifyListeners();
    }

    public void printChanges(){
        System.out.print("Changes: [");
        for (var change :
                recentChanges) {
            System.out.print("{'" + change.removedText + "' -> '" + change.replacementText + "'} ");
        }
        System.out.println("]");
    }

    public Deque<SimpleDiff> getRecentChanges() {
        return recentChanges.stream()
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    public SimpleDiff getDiffMatchingRemovedText(String text) {
        return getDiff(diff -> text.contains(diff.getRemovedText()));
    }
    public SimpleDiff getDiff(Predicate<SimpleDiff> isValid) {
        var reverseIterator = getRecentChanges().descendingIterator();
        while(reverseIterator.hasNext()) {
            var nextDiff = reverseIterator.next();
            if(isValid.test(nextDiff)){
                return nextDiff;
            }
        }
        return null;
    }

    public void resizeChangesQueue(int size){
        Queue<SimpleDiff> newQueue = EvictingQueue.create(size);
        recentChanges.forEach(change -> {
            newQueue.add(change);
        });
        recentChanges = newQueue;
        notifyListeners();
    }

    public void addChangeListener(RecentDiffsChangedListener l){
        changeListeners.add(l);
    }
    public void removeChangeListener(RecentDiffsChangedListener l){
        changeListeners.remove(l);
    }
    private void notifyListeners() {
        changeListeners.forEach(l -> {
            l.notifyChanged();
        });
    }

    public static RecentChangesService getInstance(/*Project project*/){
//        return project.getService(RecentChangesService.class);
        return ApplicationManager.getApplication().getService(RecentChangesService.class);
    }

    public void reset(){
        clearChanges();
        changeListeners = new ArrayList<>();
    }
    private void clearChanges(){
        recentChanges.clear();
        notifyListeners();
    }

    @Override
    public void dispose() {
        clearChanges();
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

    public interface RecentDiffsChangedListener{
        void notifyChanged();
    }
}
