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

    // list of registered change listeners
    private List<RecentDiffsChangedListener> changeListeners = new ArrayList<>();

    public RecentChangesService(){
        // initialize the evicting queue with the correct size
        var settings = RecentChangesSettingsService.getInstance();
        recentChanges = EvictingQueue.create(settings.getQueueSize());
        // add listener to update the queue size if it changes
        settings.addQueueSizeListener(size -> resizeChangesQueue(size));
    }

    /**
     * Adds a SimpleDiff element to the stored queue.
     * @param change The element to be added.
     */
    public void addChange(SimpleDiff change) {
        recentChanges.add(change);
        notifyListeners();
    }

    /**
     * Prints all SimpleDiff elements currently stored.
     */
    public void printChanges(){
        System.out.print("Changes: [");
        for (var change :
                recentChanges) {
            System.out.print("{'" + change.removedText + "' -> '" + change.replacementText + "'} ");
        }
        System.out.println("]");
    }

    /**
     * Gets all currently stored SimpleDiff elements.
     * @return A Deque containing all stored changes.
     */
    public Deque<SimpleDiff> getRecentChanges() {
        return recentChanges.stream()
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    /**
     * Get the first SimpleDiff of which the removed text is contained within the parameter text.
     * @param text The text to be checked for.
     * @return The first matching change.
     */
    public SimpleDiff getDiffMatchingRemovedText(String text) {
        return getDiff(diff -> text.contains(diff.getRemovedText()));
    }
    /**
     * Get the first SimpleDiff for which the Predicate can be fulfilled.
     * @param isValid The predicate to be fulfilled.
     * @return The first valid change.
     */
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

    /**
     * Replaces the current evicting queue with a new queue of capacity size.
     * @param size The size of the new queue.
     */
    public void resizeChangesQueue(int size){
        // create new queue
        Queue<SimpleDiff> newQueue = EvictingQueue.create(size);
        // transfer data to new queue
        recentChanges.forEach(change -> {
            newQueue.add(change);
        });
        // replace the old queue
        recentChanges = newQueue;
        notifyListeners();
    }

    /**
     * Adds a listener to the list of subscribed listeners.
     * @param l The listener to be added.
     */
    public void addChangeListener(RecentDiffsChangedListener l){
        changeListeners.add(l);
    }
    /**
     * Removes a listener from the list of subscribed listeners.
     * @param l The listener to be removed.
     */
    public void removeChangeListener(RecentDiffsChangedListener l){
        changeListeners.remove(l);
    }

    /**
     * Notifies all subscribed listeners that the data has changed.
     */
    private void notifyListeners() {
        changeListeners.forEach(l -> {
            l.notifyChanged();
        });
    }

    /**
     * @return The current Instance of {@link RecentChangesService}.
     */
    public static RecentChangesService getInstance(){
        return ApplicationManager.getApplication().getService(RecentChangesService.class);
    }

    /**
     * Clears the storage and removes all listeners.
     */
    public void reset(){
        clearChanges();
        changeListeners = new ArrayList<>();
    }

    /**
     * Removes all entries from the storage and notifies the listeners.
     */
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
        /**
         * Called when the data stored in the {@link RecentChangesService} has changed
         */
        void notifyChanged();
    }
}
